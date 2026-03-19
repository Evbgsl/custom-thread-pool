package evbgsl.threadpool;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/* Основной класс пула потоков:
- единый способ создавать потоки
- человеческие имена потоков
- логирование создания и заврешения
*/


public class CustomThreadPool implements CustomExecutor {
    private final int corePoolSize;
    private final BlockingQueue<Runnable> taskQueue;
    private final List<Thread> workers;
    private final CustomThreadFactory threadFactory;

    private volatile boolean shutdown;

    public CustomThreadPool(String poolName, int corePoolSize, int queueSize) {
        this.corePoolSize = corePoolSize;
        this.taskQueue = new ArrayBlockingQueue<>(queueSize);
        this.workers = new ArrayList<>();
        this.threadFactory = new CustomThreadFactory(poolName);
        this.shutdown = false;

        initWorkers();
    }

    private void initWorkers() {
        for (int i = 0; i < corePoolSize; i++) {
            Worker worker = new Worker(taskQueue, this);
            Thread thread = threadFactory.newThread(worker);
            workers.add(thread);
            thread.start();
        }
    }

    @Override
    public void execute(Runnable command) {
        if (command == null) {
            throw new NullPointerException("Task cannot be null");
        }

        if (shutdown) {
            throw new RejectedExecutionException("Thread pool is shutdown");
        }

        boolean offered = taskQueue.offer(command);
        if (!offered) {
            System.out.println("[Rejected] Task " + command + " was rejected due to overload!");
            throw new RejectedExecutionException("Task queue is full");
        }

        System.out.println("[Pool] Task accepted into queue: " + command);
    }

    @Override
    public <T> Future<T> submit(Callable<T> callable) {
        if (callable == null) {
            throw new NullPointerException("Callable cannot be null");
        }

        FutureTask<T> futureTask = new FutureTask<>(callable);
        execute(futureTask);
        return futureTask;
    }

    @Override
    public void shutdown() {
        shutdown = true;
        System.out.println("[Pool] Shutdown initiated.");
    }

    @Override
    public void shutdownNow() {
        shutdown = true;
        System.out.println("[Pool] ShutdownNow initiated.");

        for (Thread worker : workers) {
            worker.interrupt();
        }

        taskQueue.clear();
    }

    public boolean isShutdown() {
        return shutdown;
    }
}
