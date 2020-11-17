package se.tink.libraries.concurrency;

import com.google.common.base.Preconditions;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class TypedThreadPoolBuilder {
    private int corePoolSize;
    private int maximumPoolSize;
    private long keepAliveTime = 0;
    private TimeUnit keepAliveTimeUnit = TimeUnit.NANOSECONDS;
    private ThreadFactory threadFactory;

    public TypedThreadPoolBuilder(int nthreads, ThreadFactory threadFactory) {
        this.corePoolSize = nthreads;
        this.maximumPoolSize = nthreads;
        this.threadFactory = Preconditions.checkNotNull(threadFactory);
    }

    public TypedThreadPoolBuilder withMaximumPoolSize(
            int maximumPoolSize, long keepAliveTime, TimeUnit unit) {
        Preconditions.checkArgument(
                maximumPoolSize >= corePoolSize,
                "maximumPoolSize (was: %s) must be greater or equal than corePoolSize (was: %s).",
                maximumPoolSize,
                corePoolSize);
        Preconditions.checkArgument(
                keepAliveTime >= 0,
                "keepAliveTime must be zero or positive. Was: %s",
                keepAliveTime);
        Preconditions.checkNotNull(unit, "keepAliveTime unit must not be null.");

        this.maximumPoolSize = maximumPoolSize;
        this.keepAliveTime = keepAliveTime;
        this.keepAliveTimeUnit = unit;
        return this;
    }

    /**
     * Build the thread pool. This method is package protected because it uses a {@link
     * BlockingRejectedExecutionHandler} which can block a calling thread and is considered <a
     * href="http://stackoverflow.com/a/3518588/2057275">bad practise</a>. The reason why we can use
     * it here is that thread pool created by this class only will be used within an {@link
     * ListenableThreadPoolExecutor} which has its own {@link
     * java.util.concurrent.RejectedExecutionHandler}.
     *
     * @return a new thread pool executor
     */
    /*package protected*/ ThreadPoolExecutor build() {
        return new ThreadPoolExecutor(
                corePoolSize,
                maximumPoolSize,
                keepAliveTime,
                keepAliveTimeUnit,
                new SynchronousQueue<Runnable>(),
                threadFactory,
                new BlockingRejectedExecutionHandler());
    }
}
