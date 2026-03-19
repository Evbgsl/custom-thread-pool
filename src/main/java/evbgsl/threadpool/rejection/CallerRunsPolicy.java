package evbgsl.threadpool.rejection;

import evbgsl.threadpool.CustomThreadPool;

/*Если пул перегружен,
* то задача не теряется и выполняется тем потоком,
* которвый ее отправил */

public class CallerRunsPolicy implements RejectionPolicy {
    @Override
    public void reject(Runnable task, CustomThreadPool pool) {
        if (pool.isShutdown()) {
            System.out.println("[Rejected] Task " + task + " was rejected because pool is shutdown.");
            return;
        }

        System.out.println("[Rejected] Queue is overloaded. Running task in caller thread: " + task);
        task.run();
    }
}