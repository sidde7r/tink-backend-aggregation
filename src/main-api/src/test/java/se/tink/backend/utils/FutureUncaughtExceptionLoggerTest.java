package se.tink.backend.utils;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableScheduledFuture;
import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class FutureUncaughtExceptionLoggerTest {

    @Test
    public void testFutureListenerWorksWhenScheduledTaskFails() throws InterruptedException {
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        ListeningScheduledExecutorService refreshCredentialsScheduler = MoreExecutors
                .listeningDecorator(new ScheduledThreadPoolExecutor(20));
        try {
            final ListenableScheduledFuture<?> future = refreshCredentialsScheduler.schedule((Runnable) () -> {
                throw new RuntimeException("Expected to be thrown.");
            }, 1, TimeUnit.SECONDS);

            Futures.addCallback(future, new FutureCallback<Object>() {
                @Override
                public void onSuccess(@Nullable Object t) {
                    // Deliberately left empty.
                }

                @Override
                public void onFailure(Throwable throwable) {
                    countDownLatch.countDown();
                }
            });

            try {
                Assert.assertTrue(countDownLatch.await(20, TimeUnit.SECONDS));
            } finally {
                future.cancel(false);
            }
        } finally {
            refreshCredentialsScheduler.shutdown();
            Assert.assertTrue(refreshCredentialsScheduler.awaitTermination(20, TimeUnit.SECONDS));
        }
    }

    // Test [1].
    //
    // [1] https://www.cosmocode.de/en/blog/schoenborn/2009-12/17-uncaught-exceptions-in-scheduled-tasks?
    @Test
    @Ignore // Ignore this because of sleeps which are both unrealiable and slow.
    public void testScheduledUncaughtExceptions() throws InterruptedException {
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        ThreadFactory threadFactory = new ThreadFactoryBuilder()
                .setUncaughtExceptionHandler((t, e) -> countDownLatch.countDown()).build();
        ScheduledThreadPoolExecutor refreshCredentialsScheduler = new ScheduledThreadPoolExecutor(20, threadFactory);
        ScheduledFuture<?> future = refreshCredentialsScheduler.schedule((Runnable) () -> {
            throw new RuntimeException("Expected to be thrown.");
        }, 1, TimeUnit.SECONDS);

        try {
            Assert.assertTrue(countDownLatch.await(20, TimeUnit.SECONDS));
        } finally {
            future.cancel(false);
        }
    }

}
