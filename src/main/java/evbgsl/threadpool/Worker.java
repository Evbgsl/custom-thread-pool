package evbgsl.threadpool;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/* Класс исполнитель:
- сидит на очереди
- берет задачи и выполняет их
- завершает работу когда пул закрывается
*/

public class Worker implements Runnable {
    private final int id;
    private final BlockingQueue<Runnable> taskQueue;
    private final CustomThreadPool pool;
    private final long keepAliveTime;
    private final TimeUnit timeUnit;

    public Worker(
            int id,
            BlockingQueue<Runnable> taskQueue,
            CustomThreadPool pool,
            long keepAliveTime,
            TimeUnit timeUnit
    ) {
        this.id = id;
        this.taskQueue = taskQueue;
        this.pool = pool;
        this.keepAliveTime = keepAliveTime;
        this.timeUnit = timeUnit;
    }

    public boolean offerTask(Runnable task) {
        return taskQueue.offer(task);
    }

    public int getId() {
        return id;
    }

    public int getQueueSize() {
        return taskQueue.size();
    }

    public boolean isQueueEmpty() {
        return taskQueue.isEmpty();
    }

    public void clearQueue() {
        taskQueue.clear();
    }

    @Override
    public void run() {
        try {
            while (true) {
                if (pool.isShutdown() && taskQueue.isEmpty()) {
                    break;
                }

                Runnable task = taskQueue.poll(keepAliveTime, timeUnit);

                if (task != null) {
                    System.out.println("[Worker] " + Thread.currentThread().getName()
                            + " executes " + task);
                    task.run();
                    continue;
                }

                if (pool.getWorkerCount() > pool.getCorePoolSize()) {
                    System.out.println("[Worker] " + Thread.currentThread().getName()
                            + " idle timeout, stopping.");
                    break;
                }
            }
        } catch (InterruptedException e) {
            if (pool.isShutdown()) {
                Thread.currentThread().interrupt();
            }
        } catch (Exception e) {
            System.out.println("[Worker] Error: " + e.getMessage());
        } finally {
            pool.onWorkerExit(this);
        }
    }
}