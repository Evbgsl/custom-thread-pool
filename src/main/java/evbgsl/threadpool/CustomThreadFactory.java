package evbgsl.threadpool;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/* Класс с фабрикой потоков:
- единый способ создавать потоки
- человеческие имена потоков
- логирование создания и заврешения
*/

public class CustomThreadFactory implements ThreadFactory {
    private final String poolName;
    private final AtomicInteger threadCounter = new AtomicInteger(1);

    public CustomThreadFactory(String poolName) {
        this.poolName = poolName;
    }

    @Override
    public Thread newThread(Runnable r) {
        String threadName = poolName + "-worker-" + threadCounter.getAndIncrement();
        System.out.println("[ThreadFactory] Creating new thread: " + threadName);

        return new Thread(() -> {
            try {
                r.run();
            } finally {
                System.out.println("[Worker] " + Thread.currentThread().getName() + " terminated.");
            }
        }, threadName);
    }
}
