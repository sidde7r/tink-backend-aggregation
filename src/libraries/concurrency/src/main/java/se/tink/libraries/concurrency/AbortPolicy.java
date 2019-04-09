package se.tink.libraries.concurrency;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionException;

public class AbortPolicy<T> implements RejectedExecutionHandler<T> {
    @Override
    public void handle(T t, BlockingQueue<T> queue) {
        throw new RejectedExecutionException(
                "Task " + t.toString() + " rejected from " + queue.toString());
    }
}
