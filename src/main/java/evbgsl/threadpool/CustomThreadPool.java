package evbgsl.threadpool;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/* Основной класс пула потоков:
- единый способ создавать потоки
- человеческие имена потоков
- логирование создания и заврешения
*/


public class CustomThreadPool implements CustomExecutor {
    private final int corePoolSize;
    private final List<Worker> workers;
    private final List<Thread> workerThreads;
    private final AtomicInteger nextWorker = new AtomicInteger(0);
    private final CustomThreadFactory threadFactory;

    private volatile boolean shutdown;

    public CustomThreadPool(String poolName, int corePoolSize, int queueSize) {
        this.workers = new ArrayList<>();
        this.workerThreads = new ArrayList<>();
        this.threadFactory = new CustomThreadFactory(poolName);
        this.shutdown = false;
        this.corePoolSize = corePoolSize;

        initWorkers(corePoolSize, queueSize);
    }

    private void initWorkers(int corePoolSize, int queueSize) {
        for (int i = 0; i < corePoolSize; i++) {
            BlockingQueue<Runnable> queue = new ArrayBlockingQueue<>(queueSize);

            Worker worker = new Worker(i, queue, this);
            Thread thread = threadFactory.newThread(worker);

            workers.add(worker);
            workerThreads.add(thread);

            thread.start();
        }
    }

    private Worker selectWorker() {
        int index = Math.abs(nextWorker.getAndIncrement() % workers.size());
        return workers.get(index);
    }

    @Override
    public void execute(Runnable command) {
        if (command == null) {
            throw new NullPointerException("Task cannot be null");
        }

        if (shutdown) {
            throw new RejectedExecutionException("Thread pool is shutdown");
        }

        Worker worker = selectWorker();

        boolean accepted = worker.offerTask(command);

        if (!accepted) {
            System.out.println("[Rejected] Task " + command + " was rejected!");
            throw new RejectedExecutionException("Worker queue is full");
        }

        System.out.println("[Pool] Task accepted into queue #" + worker.getId() + ": " + command);
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

        for (Thread t : workerThreads) {
            t.interrupt();
        }
    }

    @Override
    public void shutdownNow() {
        shutdown = true;
        System.out.println("[Pool] ShutdownNow initiated.");

        for (Thread thread : workerThreads) {
            thread.interrupt();
        }

        for (Worker worker : workers) {
            worker.clearQueue();
        }
    }

    public boolean isShutdown() {
        return shutdown;
    }
}
