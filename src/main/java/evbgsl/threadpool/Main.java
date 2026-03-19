package evbgsl.threadpool;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) throws Exception {
        // CustomThreadPool pool = new CustomThreadPool("MyPool", 2, 5);

        CustomThreadPool pool = new CustomThreadPool(
                "MyPool",
                2,                  // corePoolSize
                4,                  // maxPoolSize
                5,                  // keepAliveTime
                TimeUnit.SECONDS,   // timeUnit
                2,                  // queueSize (маленькая, чтобы увидеть scaling)
                1                   // minSpareThreads
        );

        // несколько задач
        for (int i = 1; i <= 8; i++) {
            int taskId = i;

            pool.execute(() -> {
                System.out.println("[Task-" + taskId + "] started by " + Thread.currentThread().getName());
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                System.out.println("[Task-" + taskId + "] finished by " + Thread.currentThread().getName());
            });
        }

        // проверка Callable
        Future<Integer> future = pool.submit(() -> {
            Thread.sleep(1000);
            return 42;
        });

        System.out.println("Future result = " + future.get());

        // даем времени поработать
        Thread.sleep(8000);

        pool.shutdown();
    }
}
