package se.tink.backend.common.concurrency;

import com.google.common.util.concurrent.Uninterruptibles;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionException;

public class TypeSafeBlockingExecutionHandler<T> implements RejectedExecutionHandler<T> {
    @Override
    public void handle(T t, BlockingQueue<T> queue) throws RejectedExecutionException {
        Uninterruptibles.putUninterruptibly(queue, t);
    }
}
