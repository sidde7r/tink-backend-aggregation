package se.tink.libraries.concurrency;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionException;

public interface RejectedExecutionHandler<T> {
    /**
     * This method must <i>either</i> put an object on the queue <i>or</i> throw an exception. Not
     * doing anything is illegal and will lead to deadlocks since its caller could think that a
     * `FutureTask` was submitted and return it to its caller, who would do `FutureTask#get` on it.
     *
     * @param t
     * @param queue
     * @throws RejectedExecutionException
     */
    void handle(T t, BlockingQueue<T> queue) throws RejectedExecutionException;
}
