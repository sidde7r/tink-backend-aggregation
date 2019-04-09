package se.tink.libraries.concurrency;

import com.google.common.util.concurrent.Uninterruptibles;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

public class BlockingRejectedExecutionHandler implements RejectedExecutionHandler {

    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        Uninterruptibles.putUninterruptibly(executor.getQueue(), r);
    }
}
