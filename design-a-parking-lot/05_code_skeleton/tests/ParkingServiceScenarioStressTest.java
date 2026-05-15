import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.stream.Collectors;

/*
  Multiple client scenarios to stress-test the parking lot system.
  Structural invariants only (single source of truth).
*/
public class ParkingServiceScenarioStressTest {

    private static final Random RNG = new Random(12345);

    public static void main(String[] args) throws Exception {
        String scenario = (args.length > 0) ? args[0] : "all";
        ParkingServiceScenarioStressTest t = new ParkingServiceScenarioStressTest();

        try {
            switch (scenario) {
                case "singleThreaded":
                    t.singleThreadedSanity();
                    break;
                case "concurrentReservation":
                    t.concurrentReservationScenario();
                    break;
                case "highContention":
                    t.highContentionScenario();
                    break;
                case "mixedOpsFuzz":
                    t.mixedOpsFuzzScenario();
                    break;
                case "longRunChurn":
                    t.longRunChurnScenario();
                    break;
                case "all":
                default:
                    t.singleThreadedSanity();
                    t.concurrentReservationScenario();
                    t.highContentionScenario();
                    t.mixedOpsFuzzScenario();
                    t.longRunChurnScenario();
                    break;
            }
        } catch (AssertionError ae) {
            System.err.println("\n❌ TEST FAILURE");
            ae.printStackTrace();
            System.exit(1);
        }

        System.out.println("\n✅ ALL TESTS PASSED");
    }

    /* -------------------------------------------------- */
    /* Helpers                                            */
    /* -------------------------------------------------- */

    private User createUser(int i) {
        return new User(UUID.randomUUID(), "user-" + i, "phone-" + i);
    }

    private Vehicle createVehicleForUser(User u, int i) {
        return new Vehicle(UUID.randomUUID(), VehicleType.CAR, u.getUserId());
    }

    private ParkingLot getParkingLotViaReflection(ParkingService svc) {
        try {
            var f = ParkingService.class.getDeclaredField("parkingLot");
            f.setAccessible(true);
            return (ParkingLot) f.get(svc);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /* -------------------------------------------------- */
    /* Invariants (STRUCTURAL ONLY)                        */
    /* -------------------------------------------------- */

    private void verifyAndLogInvariants(ParkingService svc) {
        ParkingLot pl = getParkingLotViaReflection(svc);

        try {
            // -----------------------------
            // Access active sessions
            // -----------------------------
            var sessionsField =
                    ParkingService.class.getDeclaredField("currentParkingSessionsByUserId");

            sessionsField.setAccessible(true);

            @SuppressWarnings("unchecked")
            ConcurrentHashMap<UUID, ParkingSession> sessions =
                    (ConcurrentHashMap<UUID, ParkingSession>) sessionsField.get(svc);

            // -----------------------------
            // Available spots
            // -----------------------------
            Collection<ParkingSpot> available =
                    pl.getAvailableParkingSpots();

            // -----------------------------
            // Taken spots (derived from sessions)
            // -----------------------------
            Set<ParkingSpot> taken = sessions.values()
                    .stream()
                    .map(ParkingSession::getParkingSpot)
                    .collect(Collectors.toSet());

            // -----------------------------
            // Logging
            // -----------------------------
            System.out.println("\n>>> Parking Lot Snapshot");
            System.out.println("available = " + available.size());
            System.out.println("taken     = " + taken.size());

            // -----------------------------
            // 1. No overlap between
            // available and taken
            // -----------------------------
            Set<ParkingSpot> overlap = new HashSet<>(available);
            overlap.retainAll(taken);

            if (!overlap.isEmpty()) {
                throw new AssertionError(
                        "Spot(s) exist in BOTH available and taken: " + overlap
                );
            }

            // -----------------------------
            // 2. No duplicate available spots
            // -----------------------------
            Set<ParkingSpot> uniqueAvailable = new HashSet<>(available);

            if (uniqueAvailable.size() != available.size()) {
                throw new AssertionError(
                        "Duplicate spots detected in available queue"
                );
            }

            // -----------------------------
            // 3. Total spot count invariant
            // -----------------------------
            int totalSpots = available.size() + taken.size();

            final int EXPECTED_TOTAL_SPOTS = 5;

            if (totalSpots != EXPECTED_TOTAL_SPOTS) {
                throw new AssertionError(
                        "Spot leak or duplication detected. " +
                        "Expected=" + EXPECTED_TOTAL_SPOTS +
                        ", Actual=" + totalSpots
                );
            }

            // -----------------------------
            // 4. One session per user
            // -----------------------------
            Set<UUID> uniqueUsers = new HashSet<>();

            for (ParkingSession session : sessions.values()) {
                UUID uid = session.getUser().getUserId();

                if (!uniqueUsers.add(uid)) {
                    throw new AssertionError(
                            "Duplicate active sessions for user: " + uid
                    );
                }
            }

            System.out.println("Invariant check PASSED");

        } catch (AssertionError e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Invariant verification failed", e);
        }
    }

    /* -------------------------------------------------- */
    /* SCENARIO 1: Single-threaded sanity                  */
    /* -------------------------------------------------- */

    public void singleThreadedSanity() throws Exception {
        System.out.println("\n==== SINGLE-THREADED SANITY CHECK ====");
        ParkingService svc = new ParkingService();

        ParkingLot pl = getParkingLotViaReflection(svc);
        int totalSpots = pl.getAvailableParkingSpots().size();
        System.out.println("Total spots = " + totalSpots);

        List<User> users = new ArrayList<>();
        for (int i = 0; i < totalSpots + 2; i++) {
            User u = createUser(i);
            users.add(u);
            Optional<UUID> sid = svc.startParkingSession(u, createVehicleForUser(u, i));
            System.out.println("startParkingSession " + u.getName() + " -> " + sid);
        }

        verifyAndLogInvariants(svc);

        for (User u : users) {
            boolean ended = svc.endParkingSession(u);
            System.out.println("endParkingSession " + u.getName() + " -> " + ended);
        }

        verifyAndLogInvariants(svc);
        System.out.println("==== SINGLE-THREADED COMPLETE ====");
    }

    /* -------------------------------------------------- */
    /* SCENARIO 2: Concurrent reservation                 */
    /* -------------------------------------------------- */

    public void concurrentReservationScenario() throws Exception {
        System.out.println("\n==== CONCURRENT RESERVATION SCENARIO ====");
        ParkingService svc = new ParkingService();

        ParkingLot pl = getParkingLotViaReflection(svc);
        int totalSpots = pl.getAvailableParkingSpots().size();
        int numThreads = totalSpots * 4;

        ExecutorService ex = Executors.newFixedThreadPool(32);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch ready = new CountDownLatch(numThreads);

        AtomicInteger success = new AtomicInteger();
        AtomicInteger fail = new AtomicInteger();

        List<Future<String>> futures = new ArrayList<>();

        for (int i = 0; i < numThreads; i++) {
            final int id = i;
            futures.add(ex.submit(() -> {
                User u = createUser(id);
                Vehicle v = createVehicleForUser(u, id);
                ready.countDown();
                start.await();

                Optional<UUID> sid = svc.startParkingSession(u, v);
                if (sid.isPresent()) success.incrementAndGet();
                else fail.incrementAndGet();
                return "user-" + id + " -> " + sid.orElse(null);
            }));
        }

        ready.await();
        start.countDown();

        for (Future<String> f : futures) {
            System.out.println(f.get());
        }

        System.out.println("successCount=" + success.get() + ", failCount=" + fail.get());

        if (success.get() > totalSpots) {
            throw new AssertionError("More sessions than spots!");
        }

        verifyAndLogInvariants(svc);
        ex.shutdownNow();
        System.out.println("==== CONCURRENT RESERVATION COMPLETE ====");
    }

    /* -------------------------------------------------- */
    /* SCENARIO 3: High contention same user               */
    /* -------------------------------------------------- */

    public void highContentionScenario() throws Exception {
        System.out.println("\n==== HIGH CONTENTION (SAME USER) ====");
        ParkingService svc = new ParkingService();

        User u = createUser(999);
        Vehicle v = createVehicleForUser(u, 999);

        ExecutorService ex = Executors.newFixedThreadPool(32);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<String>> futures = new ArrayList<>();

        for (int i = 0; i < 50; i++) {
            futures.add(ex.submit(() -> {
                start.await();
                return svc.startParkingSession(u, v).map(UUID::toString).orElse("NO_SPOT");
            }));
        }

        start.countDown();

        Set<String> ids = new HashSet<>();
        for (Future<String> f : futures) {
            String r = f.get();
            System.out.println(r);
            if (!r.equals("NO_SPOT")) ids.add(r);
        }

        if (ids.size() > 1) {
            throw new AssertionError("Multiple sessions for same user: " + ids);
        }

        verifyAndLogInvariants(svc);
        ex.shutdownNow();
        System.out.println("==== HIGH CONTENTION COMPLETE ====");
    }

    /* -------------------------------------------------- */
    /* SCENARIO 4: Mixed ops fuzz                          */
    /* -------------------------------------------------- */

    public void mixedOpsFuzzScenario() throws Exception {
        System.out.println("\n==== MIXED OPS FUZZ SCENARIO ====");
        ParkingService svc = new ParkingService();

        List<User> users = new ArrayList<>();
        List<Vehicle> vehicles = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            User u = createUser(i + 1000);
            users.add(u);
            vehicles.add(createVehicleForUser(u, i));
        }

        ExecutorService ex = Executors.newFixedThreadPool(40);
        CountDownLatch start = new CountDownLatch(1);

        for (int t = 0; t < 40; t++) {
            ex.submit(() -> {
                start.await();
                for (int i = 0; i < 200; i++) {
                    int idx = RNG.nextInt(users.size());
                    if (RNG.nextBoolean())
                        svc.startParkingSession(users.get(idx), vehicles.get(idx));
                    else
                        svc.endParkingSession(users.get(idx));
                }
                return null;
            });
        }

        start.countDown();
        ex.shutdown();
        ex.awaitTermination(10, TimeUnit.SECONDS);

        verifyAndLogInvariants(svc);
        System.out.println("==== MIXED OPS FUZZ COMPLETE ====");
    }

    /* -------------------------------------------------- */
    /* SCENARIO 5: Long run churn                          */
    /* -------------------------------------------------- */

    public void longRunChurnScenario() throws Exception {
        System.out.println("\n==== LONG RUN CHURN (15s) ====");
        ParkingService svc = new ParkingService();

        List<User> users = new ArrayList<>();
        List<Vehicle> vehicles = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            User u = createUser(i + 2000);
            users.add(u);
            vehicles.add(createVehicleForUser(u, i));
        }

        ExecutorService ex = Executors.newFixedThreadPool(12);
        AtomicBoolean stop = new AtomicBoolean(false);

        for (int i = 0; i < 12; i++) {
            ex.submit(() -> {
                Random r = new Random();
                while (!stop.get()) {
                    int idx = r.nextInt(users.size());
                    Optional<UUID> sid = svc.startParkingSession(users.get(idx), vehicles.get(idx));
                    if (sid.isPresent()) {
                        Thread.sleep(20);
                        svc.endParkingSession(users.get(idx));
                    }
                }
                return null;
            });
        }

        Thread.sleep(15_000);
        stop.set(true);
        ex.shutdown();
        ex.awaitTermination(5, TimeUnit.SECONDS);

        verifyAndLogInvariants(svc);
        System.out.println("==== LONG RUN CHURN COMPLETE ====");
    }
}