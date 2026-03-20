package evbgsl.threadpool;

import evbgsl.threadpool.rejection.AbortPolicy;
import evbgsl.threadpool.rejection.CallerRunsPolicy;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class Main {

    public static void main(String[] args) throws Exception {
        runFinalDemo();

        // Дополнительные демонстрационные сценарии:
        // runAbortPolicyDemo();
        // runCallerRunsPolicyDemo();
        // runShutdownNowDemo();
    }

    private static void runFinalDemo() throws Exception {
        System.out.println("\n=== FINAL THREAD POOL DEMO ===");

        CustomThreadPool pool = new CustomThreadPool(
                "MyPool",
                2,       // corePoolSize
                4,                  // maxPoolSize
                5,                  // keepAliveTime
                TimeUnit.SECONDS,   // timeUnit
                1,                  // queueSize
                1,                  // minSpareThreads
                new CallerRunsPolicy()
        );

        System.out.println("[Main] Pool created.");

        System.out.println("\n--- Submitting Runnable tasks ---");
        for (int i = 1; i <= 12; i++) {
            pool.execute(new DemoTask(i, 3000));
        }

        System.out.println("\n--- Submitting Callable task ---");
        Future<Integer> future = pool.submit(() -> {
            System.out.println("[Callable] started by " + Thread.currentThread().getName());
            Thread.sleep(1000);
            System.out.println("[Callable] finished by " + Thread.currentThread().getName());
            return 42;
        });

        System.out.println("[Main] Future result = " + future.get());

        System.out.println("\n--- Waiting for workers to process tasks ---");
        Thread.sleep(10000);

        System.out.println("\n--- Calling shutdown() ---");
        pool.shutdown();

        Thread.sleep(1000);
        System.out.println("\n=== FINAL DEMO FINISHED ===");
    }

    private static void runAbortPolicyDemo() throws Exception {
        System.out.println("\n=== AbortPolicy demo ===");

        CustomThreadPool pool = new CustomThreadPool(
                "AbortPool",
                2,
                4,
                5,
                TimeUnit.SECONDS,
                1,
                1,
                new AbortPolicy()
        );

        try {
            for (int i = 1; i <= 12; i++) {
                pool.execute(new DemoTask(i, 4000));
            }
        } catch (Exception e) {
            System.out.println("[Main] Rejection caught: " + e.getMessage());
        }

        Thread.sleep(10000);
        pool.shutdown();
        Thread.sleep(1000);

        System.out.println("=== AbortPolicy demo finished ===");
    }

    private static void runCallerRunsPolicyDemo() throws Exception {
        System.out.println("\n=== CallerRunsPolicy demo ===");

        CustomThreadPool pool = new CustomThreadPool(
                "CallerRunsPool",
                2,
                4,
                5,
                TimeUnit.SECONDS,
                1,
                1,
                new CallerRunsPolicy()
        );

        for (int i = 1; i <= 12; i++) {
            pool.execute(new DemoTask(i, 4000));
        }

        Thread.sleep(10000);
        pool.shutdown();
        Thread.sleep(1000);

        System.out.println("=== CallerRunsPolicy demo finished ===");
    }

    private static void runShutdownNowDemo() throws Exception {
        System.out.println("\n=== shutdownNow demo ===");

        CustomThreadPool pool = new CustomThreadPool(
                "ShutdownNowPool",
                2,
                4,
                10,
                TimeUnit.SECONDS,
                2,
                1,
                new AbortPolicy()
        );

        for (int i = 1; i <= 8; i++) {
            pool.execute(new DemoTask(i, 5000));
        }

        Thread.sleep(2000);

        System.out.println("[Main] Calling shutdownNow()");
        pool.shutdownNow();

        Thread.sleep(1000);
        System.out.println("=== shutdownNow demo finished ===");
    }
}