import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/*
  ParkingLotStressTest.java
  - Multiple client scenarios to stress-test the parking lot system.
  - Assumes your existing classes (ParkingService, User, Vehicle, VehicleType, ParkingLot, ParkingSpot) are present.
  - Compile & run from the directory where your classes are available.
*/

public class ParkingLotStressTest {

    private static final Random RNG = new Random(12345); // deterministic seed for reproducibility

    public static void main(String[] args) throws Exception {
        String scenario = (args.length > 0) ? args[0] : "all";
        ParkingLotStressTest t = new ParkingLotStressTest();

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
    }

    // Helpers to create users/vehicles
    private User createUser(int i) {
        return new User(UUID.randomUUID(), "user-" + i, String.format("phone-%03d", i));
    }

    private Vehicle createVehicleForUser(User u, int i) {
        return new Vehicle(UUID.randomUUID(), VehicleType.CAR, u.getUserId());
    }

    /**
     * Print summary invariants using the parking lot internal maps.
     * We only rely on ParkingLot public getters (available/taken maps).
     */
    private void printInvariants(ParkingService svc) {
        ParkingLot pl = getParkingLotViaReflection(svc);
        if (pl == null) {
            System.out.println("[WARN] Cannot access ParkingLot (reflection failed). Skipping invariant checks.");
            return;
        }

        Map<Integer, ParkingSpot> avail = pl.getAvailableParkingSpots();
        Map<Integer, ParkingSpot> taken = pl.getTakenParkingSpots();

        System.out.println(">>> Parking lot snapshot:");
        System.out.println("available.size = " + avail.size());
        System.out.println("taken.size     = " + taken.size());
        System.out.println("total = " + (avail.size() + taken.size()));

        // check intersection
        Set<Integer> intersection = new HashSet<>(avail.keySet());
        intersection.retainAll(taken.keySet());
        System.out.println("intersection size (should be 0) = " + intersection.size());
        if (!intersection.isEmpty()) {
            System.out.println("Intersection keys: " + intersection);
        }

        // Check that each spot referenced in 'taken' has available=false (consistency).
        List<Integer> inconsistentTaken = taken.values().stream()
                .filter(sp -> sp.isAvailable())
                .map(ParkingSpot::getSpotId)
                .collect(Collectors.toList());
        System.out.println("taken entries that are marked available (should be 0) = " + inconsistentTaken.size());
        if (!inconsistentTaken.isEmpty()) System.out.println("inconsistentTaken = " + inconsistentTaken);

        // Check that each spot in available is marked available=true
        List<Integer> inconsistentAvailable = avail.values().stream()
                .filter(sp -> !sp.isAvailable())
                .map(ParkingSpot::getSpotId)
                .collect(Collectors.toList());
        System.out.println("available entries that are marked unavailable (should be 0) = " + inconsistentAvailable.size());
        if (!inconsistentAvailable.isEmpty()) System.out.println("inconsistentAvailable = " + inconsistentAvailable);
    }

    // Use reflection to get private parkingLot field from ParkingService (since ParkingService parkingLot is private)
    private ParkingLot getParkingLotViaReflection(ParkingService svc) {
        try {
            java.lang.reflect.Field f = ParkingService.class.getDeclaredField("parkingLot");
            f.setAccessible(true);
            return (ParkingLot) f.get(svc);
        } catch (Exception e) {
            // cannot access
            return null;
        }
    }

    /* ----------------------------
       SCENARIO 1: Single-threaded sanity checks
       - Start sessions for all users until full, then end them.
       - Check invariants in between.
       ---------------------------- */
    public void singleThreadedSanity() throws Exception {
        System.out.println("\n==== SINGLE-THREADED SANITY CHECK ====");
        ParkingService svc = new ParkingService();

        // create as many users as there are spots * 2 to also test "no available" behavior
        ParkingLot pl = getParkingLotViaReflection(svc);
        int totalSpots = (pl == null) ? 5 : (pl.getAvailableParkingSpots().size());
        System.out.println("Total spots according to ParkingLot = " + totalSpots);

        List<User> users = new ArrayList<>();
        Map<UUID, UUID> sessionIds = new HashMap<>();
        for (int i = 0; i < totalSpots + 2; i++) {
            User u = createUser(i);
            Vehicle v = createVehicleForUser(u, i);
            users.add(u);

            Optional<UUID> sid = svc.startParkingSession(u, v);
            System.out.println("startParkingSession for " + u.getName() + " -> " + sid);
            sid.ifPresent(s -> sessionIds.put(u.getUserId(), s));
        }

        printInvariants(svc);

        // end sessions
        for (User u : users) {
            boolean ended = svc.endParkingSession(u);
            System.out.println("endParkingSession for " + u.getName() + " -> " + ended);
        }

        printInvariants(svc);

        System.out.println("==== SINGLE-THREADED COMPLETE ====\n");
    }

    /* ----------------------------
       SCENARIO 2: Many threads concurrently try to start sessions (different users)
       - Use barrier to make threads start at same time.
       - Record successes and failures.
       ---------------------------- */
    public void concurrentReservationScenario() throws Exception {
        System.out.println("\n==== CONCURRENT RESERVATION SCENARIO ====");
        final ParkingService svc = new ParkingService();
        ParkingLot pl = getParkingLotViaReflection(svc);
        final int totalSpots = (pl == null) ? 5 : pl.getAvailableParkingSpots().size();
        final int numThreads = totalSpots * 4; // attempt more clients than spots

        ExecutorService ex = Executors.newFixedThreadPool(Math.min(numThreads, 64));
        CountDownLatch ready = new CountDownLatch(numThreads);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<String>> futures = new ArrayList<>();

        for (int i = 0; i < numThreads; i++) {
            final int id = i;
            futures.add(ex.submit(() -> {
                User u = createUser(id);
                Vehicle v = createVehicleForUser(u, id);

                ready.countDown();
                try { start.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }

                try {
                    Optional<UUID> sid = svc.startParkingSession(u, v);
                    return "user-" + id + " -> " + sid.map(UUID::toString).orElse("NO_SPOT");
                } catch (Exception e) {
                    return "user-" + id + " -> EXN: " + e.getClass().getSimpleName() + " : " + e.getMessage();
                }
            }));
        }

        ready.await(); // wait for threads ready
        start.countDown(); // go!

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();
        for (Future<String> f : futures) {
            String s = f.get();
            System.out.println(s);
            if (s.contains("NO_SPOT") || s.startsWith("user-") && s.contains("EXN")) {
                failCount.incrementAndGet();
            } else {
                successCount.incrementAndGet();
            }
        }

        System.out.println("successCount = " + successCount.get() + ", failCount = " + failCount.get());
        printInvariants(svc);

        ex.shutdownNow();
        System.out.println("==== CONCURRENT RESERVATION COMPLETE ====\n");
    }

    /* ----------------------------
       SCENARIO 3: High contention on same operation/spot
       - All threads attempt to start a session for the SAME user/vehicle repeatedly.
       - Good for finding race conditions around the "single active session per user" invariant.
       ---------------------------- */
    public void highContentionScenario() throws Exception {
        System.out.println("\n==== HIGH CONTENTION (SAME USER) SCENARIO ====");
        final ParkingService svc = new ParkingService();

        final User u = createUser(9999);
        final Vehicle v = createVehicleForUser(u, 9999);

        int numThreads = 50;
        ExecutorService ex = Executors.newFixedThreadPool(Math.min(50, numThreads));
        CountDownLatch ready = new CountDownLatch(numThreads);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<String>> futures = new ArrayList<>();

        for (int i = 0; i < numThreads; i++) {
            futures.add(ex.submit(() -> {
                ready.countDown();
                try { start.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }

                try {
                    Optional<UUID> sid = svc.startParkingSession(u, v);
                    return "attempt -> " + sid.map(UUID::toString).orElse("NO_SPOT");
                } catch (Exception e) {
                    return "attempt -> EXN: " + e.getMessage();
                }
            }));
        }

        ready.await();
        start.countDown();

        Set<String> sessionIds = new HashSet<>();
        int noSpot = 0;
        for (Future<String> f : futures) {
            String s = f.get();
            System.out.println(s);
            if (s.contains("NO_SPOT")) noSpot++;
            else if (s.startsWith("attempt -> ")) {
                String sid = s.substring("attempt -> ".length());
                if (!sid.equals("NO_SPOT")) sessionIds.add(sid);
            }
        }

        System.out.println("unique session ids returned for same user = " + sessionIds.size() +
                " (should be at most 1)");
        System.out.println("noSpot count = " + noSpot);
        printInvariants(svc);

        ex.shutdownNow();
        System.out.println("==== HIGH CONTENTION COMPLETE ====\n");
    }

    /* ----------------------------
       SCENARIO 4: Mixed operations fuzzing
       - Many threads perform random start/end operations on random users (with some probability).
       - Use a request log and verify final invariants.
       ---------------------------- */
    public void mixedOpsFuzzScenario() throws Exception {
        System.out.println("\n==== MIXED OPS / FUZZ SCENARIO ====");
        final ParkingService svc = new ParkingService();

        final int usersCount = 30;
        List<User> users = new ArrayList<>();
        List<Vehicle> vehicles = new ArrayList<>();
        for (int i = 0; i < usersCount; i++) {
            User u = createUser(i + 1000);
            users.add(u);
            vehicles.add(createVehicleForUser(u, i + 1000));
        }

        int threads = 40;
        ExecutorService ex = Executors.newFixedThreadPool(threads);
        CountDownLatch ready = new CountDownLatch(threads);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<String>> results = new ArrayList<>();

        for (int t = 0; t < threads; t++) {
            results.add(ex.submit(() -> {
                ready.countDown();
                try { start.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                // each thread performs a number of random ops
                for (int op = 0; op < 200; op++) {
                    int userIdx = RNG.nextInt(usersCount);
                    User u = users.get(userIdx);
                    Vehicle v = vehicles.get(userIdx);
                    int choice = RNG.nextInt(100);
                    try {
                        if (choice < 55) {
                            // start
                            svc.startParkingSession(u, v);
                        } else {
                            // end
                            svc.endParkingSession(u);
                        }
                    } catch (Exception e) {
                        // log and continue
                    }
                }
                return "thread-done";
            }));
        }

        ready.await();
        start.countDown();

        for (Future<String> f : results) {
            System.out.println(f.get());
        }

        // After fuzzing, run invariants & also try to start sessions until full to detect leaks
        printInvariants(svc);

        System.out.println("Trying to fill remaining spots (sequentially) to see behavior:");
        for (int i = 0; i < 20; i++) {
            User u = createUser(5000 + i);
            Vehicle v = createVehicleForUser(u, 5000 + i);
            Optional<UUID> sid = svc.startParkingSession(u, v);
            System.out.println("fill attempt " + i + " -> " + sid);
        }

        printInvariants(svc);
        ex.shutdownNow();
        System.out.println("==== MIXED OPS FUZZ COMPLETE ====\n");
    }

    /* ----------------------------
       SCENARIO 5: Long-running churn
       - Threads continually start -> wait random -> end sessions to simulate real workload.
       - Run for a configurable duration and watch for deadlocks, starvation, or resource leaks.
       ---------------------------- */
    public void longRunChurnScenario() throws Exception {
        System.out.println("\n==== LONG-RUN CHURN SCENARIO (15s) ====");
        final ParkingService svc = new ParkingService();

        final int usersCount = 20;
        List<User> users = new ArrayList<>();
        List<Vehicle> vehicles = new ArrayList<>();
        for (int i = 0; i < usersCount; i++) {
            User u = createUser(i + 2000);
            users.add(u);
            vehicles.add(createVehicleForUser(u, i + 2000));
        }

        int threads = 12;
        ExecutorService ex = Executors.newFixedThreadPool(threads);
        final AtomicBoolean stopFlag = new AtomicBoolean(false);

        for (int t = 0; t < threads; t++) {
            ex.submit(() -> {
                Random r = new Random(RNG.nextInt());
                while (!stopFlag.get()) {
                    int idx = r.nextInt(usersCount);
                    User u = users.get(idx);
                    Vehicle v = vehicles.get(idx);
                    try {
                        Optional<UUID> sid = svc.startParkingSession(u, v);
                        // hold spot for 10-200 ms if obtained
                        if (sid.isPresent()) {
                            Thread.sleep(10 + r.nextInt(190));
                            svc.endParkingSession(u);
                        } else {
                            // back off a bit
                            Thread.sleep(5 + r.nextInt(50));
                        }
                    } catch (Exception e) {
                        // log occasionally
                    }
                }
            });
        }

        // run for 15 seconds
        Thread.sleep(15_000);
        // stop threads
        stopFlag.set(true);
        ex.shutdown();
        ex.awaitTermination(5, TimeUnit.SECONDS);

        printInvariants(svc);
        System.out.println("==== LONG RUN CHURN COMPLETE ====\n");
    }
}