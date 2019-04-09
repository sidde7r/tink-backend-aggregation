package se.tink.libraries.concurrency;

import com.google.common.util.concurrent.Uninterruptibles;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.framework.recipes.locks.InterProcessReadWriteLock;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class TransactionInMemoryReadWriteLockTest {

    @Test
    @Ignore
    public void testRecreatingRaceCondition() throws Exception {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(2000, 5);
        CuratorFramework client = CuratorFrameworkFactory.newClient("127.0.0.1:2181", retryPolicy);
        client.start();

        final InterProcessReadWriteLock rwLock =
                new InterProcessReadWriteLock(client, "/jens/super/lock");

        final CountDownLatch firstReadLockTaken = new CountDownLatch(1);
        final CountDownLatch writeLockTaken = new CountDownLatch(1);
        final CountDownLatch readyToShutDown = new CountDownLatch(1);

        Thread firstReadLockThread =
                new Thread(
                        () -> {
                            final InterProcessMutex rLock = rwLock.readLock();
                            try {
                                rLock.acquire();
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }

                            firstReadLockTaken.countDown();
                            try {
                                writeLockTaken.await();
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }

                            try {
                                final InterProcessMutex wLock = rwLock.readLock();
                                try {
                                    wLock.acquire();
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                                try {
                                    Uninterruptibles.awaitUninterruptibly(
                                            readyToShutDown, 2, TimeUnit.MINUTES);
                                } finally {
                                    if (rLock.isAcquiredInThisProcess()) {
                                        try {
                                            wLock.release();
                                        } catch (Exception e) {
                                            throw new RuntimeException(e);
                                        }
                                    }
                                }

                            } finally {
                                if (rLock.isAcquiredInThisProcess()) {
                                    try {
                                        rLock.release();
                                    } catch (Exception e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                            }
                        });
        Thread writeLockThread =
                new Thread(
                        () -> {
                            final InterProcessMutex lock = rwLock.writeLock();
                            try {
                                lock.acquire();
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                            try {
                                writeLockTaken.countDown();
                                Uninterruptibles.awaitUninterruptibly(
                                        readyToShutDown, 2, TimeUnit.MINUTES);
                            } finally {
                                if (lock.isAcquiredInThisProcess()) {
                                    try {
                                        lock.release();
                                    } catch (Exception e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                            }
                        });

        firstReadLockThread.start();
        firstReadLockTaken.await();
        writeLockThread.start();

        readyToShutDown.countDown();

        firstReadLockThread.join(20000);
        writeLockThread.join(20000);

        Assert.assertFalse(firstReadLockThread.isAlive());
        Assert.assertFalse(writeLockThread.isAlive());
    }
}
