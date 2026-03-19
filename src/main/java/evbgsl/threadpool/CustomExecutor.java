package evbgsl.threadpool;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;

/* Кастомный интерфейс CustomExecutor
любой класс, который называет себя CustomExecutor, обязан уметь:
- выполнять Runnable
- принимать Callable
- завершаться мягко
- завершаться резко
*/

public interface CustomExecutor extends Executor {
    @Override
    void execute(Runnable command);

    <T> Future<T> submit(Callable<T> callable);

    void shutdown();

    void shutdownNow();
}