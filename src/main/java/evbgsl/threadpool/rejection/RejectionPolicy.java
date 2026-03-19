package evbgsl.threadpool.rejection;

import evbgsl.threadpool.CustomThreadPool;

// eсли пул не смог принять задачу, он вызывает reject
public interface RejectionPolicy {
    void reject(Runnable task, CustomThreadPool pool);
}
