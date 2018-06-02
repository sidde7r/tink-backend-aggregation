package se.tink.backend.common.concurrency;

import java.util.concurrent.TimeUnit;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.barriers.DistributedBarrier;
import org.apache.curator.framework.recipes.locks.InterProcessLock;
import org.apache.curator.framework.recipes.locks.InterProcessReadWriteLock;
import org.apache.curator.framework.recipes.locks.InterProcessSemaphoreMutex;
import se.tink.backend.common.coordination.BarrierName;
import se.tink.backend.common.exceptions.LockException;
import se.tink.backend.common.utils.LogUtils;

/*
Scenario for generate statistics and activities:
1. Call `prepareForGeneration()` before calling method for generating statistics and activities.
   This method will mark, that we are going to generate statistics and activities (in another thread).
2. Call `lockForGeneration()` before access to shared modified data.
3. Call `releaseForRead()` if statistics and activities are ready for fetching, but we need to do some more actions.
4. Call `releaseAfterGeneration()` after finishing to modify or having access to shared data.

Scenario for fetching statistics and activities:
1. Call `waitForRead()` before fetching statistics or activities
 */
public class StatisticsActivitiesLock {
    private static final String LOCK_PREFIX_USER_WRITE = "/locks/generateStatisticsAndActivities/user/write/";
    private static final String LOCK_PREFIX_USER_READ = "/locks/generateStatisticsAndActivities/user/read/";
    private static final LogUtils log = new LogUtils(StatisticsActivitiesLock.class);

    private final String userId;
    private final DistributedBarrier barrier;
    private final InterProcessSemaphoreMutex writeLock;
    private final InterProcessReadWriteLock readWriteLock;

    public StatisticsActivitiesLock(CuratorFramework coordinationClient, String userId) {
        this.userId = userId;
        this.barrier = getBarrier(coordinationClient, userId);
        this.writeLock = getWriteLock(coordinationClient, userId);
        this.readWriteLock = getReadWriteLock(coordinationClient, userId);
    }

    public void prepareForGeneration() {
        getBarrier();
    }

    public void lockForGeneration() throws Exception {
        acquireForRead();
        removeBarrier();
        acquireForWrite();
    }

    public void releaseForRead() {
        releaseLock(readWriteLock.readLock(), true);
    }

    public void releaseAfterGeneration() {
        removeBarrier();
        releaseForReadUncheck();
        releaseForWrite();
    }

    public boolean waitForRead(long time, TimeUnit unit) throws LockException {
        try {
            if (!barrier.waitOnBarrier(10, TimeUnit.SECONDS) || !readWriteLock.writeLock().acquire(time, unit)) {
                return false;
            } else {
                // We successfully acquire lock and release it now for to not acquire time when we get statistics.
                // Since we save statistics as a BLOB, we will get consistent data
                readWriteLock.writeLock().release();
                return true;
            }
        } catch (Exception e) {
            throw new LockException(e);
        }
    }

    private void getBarrier() {
        try {
            barrier.setBarrier();
        } catch (Exception e) {
            log.error(userId, "Could not set statistics barrier", e);
        }
    }

    private void removeBarrier() {
        try {
            barrier.removeBarrier();
        } catch (Exception e) {
            log.error(userId, "Could not remove barrier", e);
        }
    }

    private void acquireForRead() throws LockException {
        try {
            readWriteLock.readLock().acquire();
        } catch (Exception e) {
            throw new LockException(e);
        }
    }

    private void releaseForReadUncheck() {
        releaseLock(readWriteLock.readLock(), false);
    }

    private void acquireForWrite() {
        try {
            if (!writeLock.acquire(1, TimeUnit.MINUTES)) {
                log.warn(userId,
                        "Could not aquire write lock when generating statistics/activities, but proceeding anyways");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void releaseForWrite() {
        releaseLock(writeLock, true);
    }

    private void releaseLock(InterProcessLock lock, boolean checkRunning) {
        if (lock.isAcquiredInThisProcess()) {
            try {
                lock.release();
            } catch (Exception e) {
                log.error(userId, "Could not release a lock: " + lock.toString(), e);
            }
        } else if (checkRunning) {
            log.warn(userId, "Lock was not acquired in this process: " + lock.toString());
        }
    }

    /**
     * Barrier, which marks that we are going to generate statistics (in another thread)
     * Should be set before calling to generate statistics in new thread and removed after we acquire `readLock`.
     */
    private DistributedBarrier getBarrier(CuratorFramework coordinationClient, String userId) {
        return new DistributedBarrier(coordinationClient,
                BarrierName.build(BarrierName.Prefix.STATISTICS_ACTIVITIES_USER_BARRIER, userId));
    }

    /**
     * Common lock for generating statistics and activities, which allows to recalculate statistics/activities only in one thread.
     * Should be set before calculation and removed after it.
     */
    private InterProcessSemaphoreMutex getWriteLock(CuratorFramework coordinationClient, String userId) {
        return new InterProcessSemaphoreMutex(coordinationClient, LOCK_PREFIX_USER_WRITE + userId);
    }

    /**
     * Lock allows to get statistics and activities only if all `readLock`s are released.
     * `readLock` should be set before any operations and removed after we are ready to share data.
     * `writeLock` waits while all `readLock`s are released. writeLock` should be set and released
     * before we get statistics/activities
     */
    private InterProcessReadWriteLock getReadWriteLock(CuratorFramework coordinationClient, String userId) {
        return new InterProcessReadWriteLock(coordinationClient, LOCK_PREFIX_USER_READ + userId);
    }

}
