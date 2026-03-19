package evbgsl.threadpool;

import java.util.concurrent.BlockingQueue;

/* Класс исполнитель:
- сидит на очереди
- берет задачи и выполняет их
- завершает работу когда пул закрывается
*/

public class Worker implements Runnable {
    private final int id;
    private final BlockingQueue<Runnable> taskQueue;
    private final CustomThreadPool pool;

    public Worker(int id, BlockingQueue<Runnable> taskQueue, CustomThreadPool pool) {
        this.id = id;
        this.taskQueue = taskQueue;
        this.pool = pool;
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

    public void clearQueue() {
        taskQueue.clear();
    }

    @Override
    public void run() {
        while (true) {
            if (pool.isShutdown() && taskQueue.isEmpty()) {
                break;
            }

            try {
                Runnable task = taskQueue.take();
                System.out.println("[Worker] " + Thread.currentThread().getName()
                        + " executes " + task);
                task.run();
            } catch (InterruptedException e) {
                if (pool.isShutdown()) {
                    Thread.currentThread().interrupt();
                    break;
                }
            } catch (Exception e) {
                System.out.println("[Worker] Error while executing task: " + e.getMessage());
            }
        }
    }
}