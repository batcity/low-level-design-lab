import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.stream.Collectors;

public class ParkingServiceConcurrencyInvariantTest {

    public static void main(String[] args) throws Exception {
        try {
            leakAndReuseTest();
            midFlightInvariantObserver();
            reparkSameUserTest();
            concurrentChurnObserverTest();
        } catch (AssertionError ae) {
            System.err.println("\n❌ CONCURRENCY FAILURE DETECTED");
            ae.printStackTrace();
            System.exit(1);
        }

        System.out.println("\n  ALL TESTS PASSED!");
    }

    /* -------------------------------------------------- */
    /* Helpers                                            */
    /* -------------------------------------------------- */

    private static User user(int i) {
        return new User(UUID.randomUUID(), "user-" + i, "p");
    }

    private static Vehicle vehicle(User u) {
        return new Vehicle(UUID.randomUUID(), VehicleType.CAR, u.getUserId());
    }

    private static ParkingLot lot(ParkingService svc) {
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

    private static void assertInvariants(ParkingService svc, ParkingLot pl) {

        try {
            // ------------------------------------------------
            // Access active sessions via reflection
            // ------------------------------------------------
            var sessionsField =
                    ParkingService.class.getDeclaredField(
                            "currentParkingSessionsByUserId"
                    );

            sessionsField.setAccessible(true);

            @SuppressWarnings("unchecked")
            ConcurrentHashMap<UUID, ParkingSession> sessions =
                    (ConcurrentHashMap<UUID, ParkingSession>)
                            sessionsField.get(svc);

            // ------------------------------------------------
            // Available spots
            // ------------------------------------------------
            Collection<ParkingSpot> available =
                    pl.getAvailableParkingSpots();

            // ------------------------------------------------
            // Taken spots
            // ------------------------------------------------
            Set<ParkingSpot> taken = sessions.values()
                    .stream()
                    .map(ParkingSession::getParkingSpot)
                    .collect(Collectors.toSet());

            // ------------------------------------------------
            // Snapshot logging
            // ------------------------------------------------
            System.out.println(
                    "avail=" + available.size() +
                    ", taken=" + taken.size()
            );

            // ------------------------------------------------
            // 1. No overlap
            // ------------------------------------------------
            Set<ParkingSpot> overlap = new HashSet<>(available);
            overlap.retainAll(taken);

            if (!overlap.isEmpty()) {
                throw new AssertionError(
                        "Spot(s) exist in BOTH available and taken: " + overlap
                );
            }

            // ------------------------------------------------
            // 2. No duplicate available spots
            // ------------------------------------------------
            Set<ParkingSpot> uniqueAvailable =
                    new HashSet<>(available);

            if (uniqueAvailable.size() != available.size()) {
                throw new AssertionError(
                        "Duplicate spots detected in available queue"
                );
            }

            // ------------------------------------------------
            // 3. No duplicate taken spots
            // ------------------------------------------------
            int activeSessions = sessions.size();

            if (taken.size() != activeSessions) {
                throw new AssertionError(
                        "Two sessions share the same parking spot. " +
                        "sessions=" + activeSessions +
                        ", uniqueTaken=" + taken.size()
                );
            }

            // ------------------------------------------------
            // 4. Total spot accounting
            // ------------------------------------------------
            final int EXPECTED_TOTAL_SPOTS = 5;

            int total = available.size() + taken.size();

            if (total != EXPECTED_TOTAL_SPOTS) {
                throw new AssertionError(
                        "Spot leak or duplication detected. " +
                        "Expected=" + EXPECTED_TOTAL_SPOTS +
                        ", Actual=" + total
                );
            }

            // ------------------------------------------------
            // 5. One session per user
            // ------------------------------------------------
            Set<UUID> uniqueUsers = new HashSet<>();

            for (ParkingSession session : sessions.values()) {

                UUID uid = session.getUser().getUserId();

                if (!uniqueUsers.add(uid)) {
                    throw new AssertionError(
                            "Duplicate active sessions for user: " + uid
                    );
                }
            }

        } catch (AssertionError e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(
                    "Invariant verification failed",
                    e
            );
        }
    }

    private static int expectedTotalSpots(ParkingLot pl) {
        return pl.getAvailableParkingSpots().size();
    }

    /* -------------------------------------------------- */
    /* TEST 1: Leak + reuse                                */
    /* -------------------------------------------------- */

    private static void leakAndReuseTest() throws Exception {
        System.out.println("\n==== LEAK & REUSE TEST ====");

        ParkingService svc = new ParkingService();
        ParkingLot pl = lot(svc);

        int totalSpots = pl.getAvailableParkingSpots().size();

        User u = user(1);
        Vehicle v = vehicle(u);

        for (int i = 0; i < totalSpots + 1; i++) {
            Optional<UUID> sid = svc.startParkingSession(u, v);

            if (sid.isEmpty()) {
                throw new AssertionError(
                    "Parking spot leak detected at iteration " + i +
                    " (total spots = " + totalSpots + ")"
                );
            }

            svc.endParkingSession(u);
        }

        assertInvariants(svc, pl);
        System.out.println("PASS: Parking spots reused correctly, no leak detected.");
    }

    /* -------------------------------------------------- */
    /* TEST 2: Observe invariants DURING mutation          */
    /* -------------------------------------------------- */

    private static void midFlightInvariantObserver() throws Exception {
        System.out.println("\n==== MID-FLIGHT INVARIANT OBSERVER ====");
        ParkingService svc = new ParkingService();
        ParkingLot pl = lot(svc);

        ExecutorService ex = Executors.newFixedThreadPool(8);
        AtomicBoolean stop = new AtomicBoolean(false);

        // mutators
        for (int t = 0; t < 4; t++) {
            ex.submit(() -> {
                int i = 0;
                while (!stop.get()) {
                    User u = user(i++);
                    Vehicle v = vehicle(u);
                    svc.startParkingSession(u, v);
                }
                return null;
            });
        }

        // observer
        ex.submit(() -> {
            for (int i = 0; i < 50_000; i++) {
                assertInvariants(svc, pl);
            }
            return null;
        });

        ex.shutdown();
        ex.awaitTermination(5, TimeUnit.SECONDS);
        stop.set(true);
    }

    /* -------------------------------------------------- */
    /* TEST 3: Same user re-park                           */
    /* -------------------------------------------------- */

    private static void reparkSameUserTest() throws Exception {
        System.out.println("\n==== SAME USER RE-PARK TEST ====");
        ParkingService svc = new ParkingService();

        User u = user(999);
        Vehicle v = vehicle(u);

        if (svc.startParkingSession(u, v).isEmpty())
            throw new AssertionError("Initial park failed");

        svc.endParkingSession(u);

        if (svc.startParkingSession(u, v).isEmpty())
            throw new AssertionError("User cannot re-park after end");
    }

    /* -------------------------------------------------- */
    /* TEST 4: Concurrent churn + observation              */
    /* -------------------------------------------------- */

    private static void concurrentChurnObserverTest() throws Exception {
        System.out.println("\n==== CONCURRENT CHURN OBSERVER ====");
        ParkingService svc = new ParkingService();
        ParkingLot pl = lot(svc);

        List<User> users = new ArrayList<>();
        List<Vehicle> vehicles = new ArrayList<>();

        for (int i = 0; i < 20; i++) {
            User u = user(i);
            users.add(u);
            vehicles.add(vehicle(u));
        }

        ExecutorService ex = Executors.newFixedThreadPool(16);
        AtomicBoolean stop = new AtomicBoolean(false);

        // churn
        for (int i = 0; i < 12; i++) {
            ex.submit(() -> {
                Random r = new Random();
                while (!stop.get()) {
                    int idx = r.nextInt(users.size());
                    if (r.nextBoolean())
                        svc.startParkingSession(users.get(idx), vehicles.get(idx));
                    else
                        svc.endParkingSession(users.get(idx));
                }
                return null;
            });
        }

        // invariant checker
        ex.submit(() -> {
            for (int i = 0; i < 100_000; i++) {
                assertInvariants(svc, pl);
            }
            return null;
        });

        Thread.sleep(3_000);
        stop.set(true);

        ex.shutdown();
        ex.awaitTermination(5, TimeUnit.SECONDS);
    }
}