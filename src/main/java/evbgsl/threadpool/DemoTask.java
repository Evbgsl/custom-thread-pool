package evbgsl.threadpool;

public class DemoTask implements Runnable {
    private final int taskId;
    private final long durationMs;

    public DemoTask(int taskId, long durationMs) {
        this.taskId = taskId;
        this.durationMs = durationMs;
    }

    @Override
    public void run() {
        System.out.println("[Task-" + taskId + "] started by " + Thread.currentThread().getName());
        try {
            Thread.sleep(durationMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("[Task-" + taskId + "] interrupted in " + Thread.currentThread().getName());
            return;
        }
        System.out.println("[Task-" + taskId + "] finished by " + Thread.currentThread().getName());
    }

    @Override
    public String toString() {
        return "Task-" + taskId;
    }
}