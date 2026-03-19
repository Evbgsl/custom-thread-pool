package evbgsl.threadpool;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import evbgsl.threadpool.rejection.RejectionPolicy;

/* Основной класс пула потоков:
- единый способ создавать потоки
- человеческие имена потоков
- логирование создания и заврешения
*/


public class CustomThreadPool implements CustomExecutor {
    private final int corePoolSize;
    private final int maxPoolSize;
    private final long keepAliveTime;
    private final TimeUnit timeUnit;
    private final int queueSize;
    private final int minSpareThreads;

    private final List<Worker> workers;
    private final List<Thread> workerThreads;
    private final CustomThreadFactory threadFactory;
    private final AtomicInteger nextWorker = new AtomicInteger(0);

    private volatile boolean shutdown;

    private final RejectionPolicy rejectionPolicy;

    public CustomThreadPool(
            String poolName,
            int corePoolSize,
            int maxPoolSize,
            long keepAliveTime,
            TimeUnit timeUnit,
            int queueSize,
            int minSpareThreads,
            RejectionPolicy rejectionPolicy
    ) {
        if (corePoolSize <= 0) {
            throw new IllegalArgumentException("corePoolSize must be > 0");
        }
        if (maxPoolSize < corePoolSize) {
            throw new IllegalArgumentException("maxPoolSize must be >= corePoolSize");
        }
        if (queueSize <= 0) {
            throw new IllegalArgumentException("queueSize must be > 0");
        }
        if (minSpareThreads < 0) {
            throw new IllegalArgumentException("minSpareThreads must be >= 0");
        }
        if (rejectionPolicy == null) {
            throw new IllegalArgumentException("rejectionPolicy must not be null");
        }

        this.corePoolSize = corePoolSize;
        this.maxPoolSize = maxPoolSize;
        this.keepAliveTime = keepAliveTime;
        this.timeUnit = timeUnit;
        this.queueSize = queueSize;
        this.minSpareThreads = minSpareThreads;
        this.rejectionPolicy = rejectionPolicy;

        this.workers = new ArrayList<>();
        this.workerThreads = new ArrayList<>();
        this.threadFactory = new CustomThreadFactory(poolName);
        this.shutdown = false;

        initCoreWorkers();
    }

    private void initCoreWorkers() {
        for (int i = 0; i < corePoolSize; i++) {
            addWorker();
        }
    }

    private synchronized void addWorker() {
        int workerId = workers.size();
        BlockingQueue<Runnable> queue = new ArrayBlockingQueue<>(queueSize);

        Worker worker = new Worker(
                workerId,
                queue,
                this,
                keepAliveTime,
                timeUnit
        );

        Thread thread = threadFactory.newThread(worker);

        workers.add(worker);
        workerThreads.add(thread);
        thread.start();

        System.out.println("[Pool] Added new worker #" + workerId);
    }

    private Worker selectWorker() {
        int index = Math.abs(nextWorker.getAndIncrement() % workers.size());
        return workers.get(index);
    }

    @Override
    public synchronized void execute(Runnable command) {
        if (command == null) {
            throw new NullPointerException("Task cannot be null");
        }

        if (shutdown) {
            rejectionPolicy.reject(command, this);
            return;
        }

        ensureMinSpareThreads();

        Worker worker = selectWorker();
        boolean accepted = worker.offerTask(command);

        if (!accepted) {
            if (getWorkerCount() < maxPoolSize) {
                addWorker();
                Worker newWorker = workers.get(workers.size() - 1);

                if (newWorker.offerTask(command)) {
                    System.out.println("[Pool] Task accepted into new worker queue #" + newWorker.getId() + ": " + command);
                    return;
                }
            }

            rejectionPolicy.reject(command, this);
            return;
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

    @Override
    public String toString() {
        return "CustomThreadPool{" +
                "corePoolSize=" + corePoolSize +
                ", maxPoolSize=" + maxPoolSize +
                ", workerCount=" + getWorkerCount() +
                ", shutdown=" + shutdown +
                '}';
    }


    public boolean isShutdown() {
        return shutdown;
    }

    public int getCorePoolSize() {
        return corePoolSize;
    }

    public synchronized int getWorkerCount() {
        return workers.size();
    }

    public int getMinSpareThreads() {
        return minSpareThreads;
    }

    public synchronized int getIdleWorkerCount() {
        int idle = 0;
        for (Worker worker : workers) {
            if (worker.isQueueEmpty()) {
                idle++;
            }
        }
        return idle;
    }

    public synchronized void onWorkerExit(Worker worker) {
        int index = workers.indexOf(worker);
        if (index >= 0) {
            workers.remove(index);
            workerThreads.remove(index);
            System.out.println("[Pool] Worker #" + worker.getId() + " removed from pool");
        }
    }

    private synchronized void ensureMinSpareThreads() {
        while (getIdleWorkerCount() < minSpareThreads && getWorkerCount() < maxPoolSize) {
            addWorker();
        }
    }
}
