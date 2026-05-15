/*
 * Simple throughput(operations completed per unit time) benchmark for ParkingService.
 *
 * Measures how long the service takes to process a large
 * number of concurrent parking attempts using a fixed-size
 * thread pool.
 *
 * Focus:
 * - contention overhead
 * - scalability characteristics
 * - concurrent collection performance
 *
 * NOTE:
 * This benchmark does NOT validate correctness.
 * It only measures execution time under parallel load.
 *
 * Also note:
 * the parking lot contains only 5 spots, so after the
 * lot fills up, most operations fail fast. The benchmark
 * therefore primarily measures concurrent contention and
 * rejection-path performance rather than sustained parking throughput.
 */
import java.util.*;
import java.util.concurrent.*;

public class ParkingServiceThroughputBenchmark {

    static final int THREADS = 32;
    static final int OPS = 50_000;

    public static void main(String[] args) throws Exception {
        System.out.println("Warmup...");
        run(new ParkingService(), false);

        System.out.println("\n=== BENCHMARK ===");
        run(new ParkingService(), true);
    }

    static void run(Object svc, boolean print) throws Exception {

        ExecutorService ex = Executors.newFixedThreadPool(THREADS);
        CountDownLatch start = new CountDownLatch(1);

        List<User> users = new ArrayList<>();
        List<Vehicle> vehicles = new ArrayList<>();

        for (int i = 0; i < OPS; i++) {
            User u = new User(UUID.randomUUID(), "u" + i, "p");
            users.add(u);
            vehicles.add(new Vehicle(UUID.randomUUID(), VehicleType.CAR, u.getUserId()));
        }

        long begin = System.nanoTime();

        for (int i = 0; i < OPS; i++) {
            final int idx = i;
            ex.submit(() -> {
                start.await();
                ((ParkingService) svc)
                            .startParkingSession(users.get(idx), vehicles.get(idx));
                return null;
            });
        }

        start.countDown();
        ex.shutdown();
        ex.awaitTermination(30, TimeUnit.SECONDS);

        long end = System.nanoTime();
        long ms = TimeUnit.NANOSECONDS.toMillis(end - begin);

        if (print) {
            System.out.printf(
                    "%s took %d ms%n",
                    svc.getClass().getSimpleName(),
                    ms
            );
        }
    }
}
