package evbgsl.threadpool;

import java.util.concurrent.Future;

public class Main {
    public static void main(String[] args) throws Exception {
        CustomThreadPool pool = new CustomThreadPool("MyPool", 2, 5);

        for (int i = 1; i <= 5; i++) {
            int taskId = i;
            pool.execute(() -> {
                System.out.println("[Task-" + taskId + "] started by " + Thread.currentThread().getName());
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                System.out.println("[Task-" + taskId + "] finished by " + Thread.currentThread().getName());
            });
        }

        Future<Integer> future = pool.submit(() -> {
            Thread.sleep(1000);
            return 42;
        });

        System.out.println("Future result = " + future.get());

        Thread.sleep(7000);
        pool.shutdown();
    }
}
