import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.stream.Collectors;

public class ParkingLotConcurrencyStressTest {

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

    private static void assertInvariants(ParkingLot pl) {
        Map<Integer, ParkingSpot> avail = pl.getAvailableParkingSpots();
        Map<Integer, ParkingSpot> taken = pl.getTakenParkingSpots();

        // no intersection
        Set<Integer> both = new HashSet<>(avail.keySet());
        both.retainAll(taken.keySet());
        if (!both.isEmpty()) {
            throw new AssertionError("Spot in BOTH maps: " + both);
        }

        // taken must be unavailable
        List<Integer> badTaken = taken.values().stream()
                .filter(ParkingSpot::isAvailable)
                .map(ParkingSpot::getSpotId)
                .collect(Collectors.toList());
        if (!badTaken.isEmpty()) {
            throw new AssertionError("Taken marked available: " + badTaken);
        }

        // available must be available
        List<Integer> badAvail = avail.values().stream()
                .filter(ps -> !ps.isAvailable())
                .map(ParkingSpot::getSpotId)
                .collect(Collectors.toList());
        if (!badAvail.isEmpty()) {
            throw new AssertionError("Available marked unavailable: " + badAvail);
        }
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
                    "Parking spot leak detected: no spot available at iteration " + i +
                    " (total spots = " + totalSpots + ")"
                );
            }

            svc.endParkingSession(u);
        }

        // If we reach here, reuse worked correctly
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
                assertInvariants(pl);
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
                assertInvariants(pl);
            }
            return null;
        });

        Thread.sleep(3_000);
        stop.set(true);

        ex.shutdown();
        ex.awaitTermination(5, TimeUnit.SECONDS);
    }
}