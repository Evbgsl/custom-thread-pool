package evbgsl.threadpool.rejection;

import evbgsl.threadpool.CustomThreadPool;

import java.util.concurrent.RejectedExecutionException;

/* Жесткая политика:
* - задача не примется
* - вызовется исклбючение
* - перегрузка явно видна
* */

public class AbortPolicy implements RejectionPolicy {
    @Override
    public void reject(Runnable task, CustomThreadPool pool) {
        System.out.println("[Rejected] Task " + task + " was rejected due to overload!");
        throw new RejectedExecutionException("Task " + task + " rejected from " + pool);
    }
}
