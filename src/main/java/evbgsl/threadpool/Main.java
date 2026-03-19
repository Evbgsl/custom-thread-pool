package evbgsl.threadpool;

import evbgsl.threadpool.rejection.AbortPolicy;
import evbgsl.threadpool.rejection.CallerRunsPolicy;
import evbgsl.threadpool.rejection.RejectionPolicy;

import java.util.concurrent.TimeUnit;

public class Main {

    public static void main(String[] args) throws Exception {
        // runAbortPolicyDemo();
        runCallerRunsPolicyDemo();
    }

    private static void runAbortPolicyDemo() throws Exception {
        System.out.println("\n=== AbortPolicy demo ===");

        runDemo(
                new AbortPolicy(),
                "AbortPool",
                2,
                4,
                5,
                TimeUnit.SECONDS,
                1,
                1,
                12,
                4000
        );
    }

    private static void runCallerRunsPolicyDemo() throws Exception {
        System.out.println("\n=== CallerRunsPolicy demo ===");

        runDemo(
                new CallerRunsPolicy(),
                "CallerRunsPool",
                2,
                4,
                5,
                TimeUnit.SECONDS,
                1,
                1,
                12,
                4000
        );
    }

    private static void runDemo(
            RejectionPolicy rejectionPolicy,
            String poolName,
            int corePoolSize,
            int maxPoolSize,
            long keepAliveTime,
            TimeUnit timeUnit,
            int queueSize,
            int minSpareThreads,
            int taskCount,
            long taskSleepMs
    ) throws Exception {

        CustomThreadPool pool = new CustomThreadPool(
                poolName,
                corePoolSize,
                maxPoolSize,
                keepAliveTime,
                timeUnit,
                queueSize,
                minSpareThreads,
                rejectionPolicy
        );

        try {
            for (int i = 1; i <= taskCount; i++) {
                int taskId = i;
                pool.execute(() -> {
                    System.out.println("[Task-" + taskId + "] started by " + Thread.currentThread().getName());
                    try {
                        Thread.sleep(taskSleepMs);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        System.out.println("[Task-" + taskId + "] interrupted in " + Thread.currentThread().getName());
                        return;
                    }
                    System.out.println("[Task-" + taskId + "] finished by " + Thread.currentThread().getName());
                });
            }
        } catch (Exception e) {
            System.out.println("[Main] Rejection caught: " + e.getMessage());
        }

        Thread.sleep(10000);
        pool.shutdown();

        Thread.sleep(1000);
        System.out.println("=== Demo finished ===");
    }
}