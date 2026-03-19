package evbgsl.threadpool;

import java.util.concurrent.BlockingQueue;

/* Класс исполнитель:
- сидит на очереди
- берет задачи и выполняет их
- завершает работу когда пул закрывается
*/

public class Worker implements Runnable {
    private final BlockingQueue<Runnable> taskQueue;
    private final CustomThreadPool pool;

    public Worker(BlockingQueue<Runnable> taskQueue, CustomThreadPool pool) {
        this.taskQueue = taskQueue;
        this.pool = pool;
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