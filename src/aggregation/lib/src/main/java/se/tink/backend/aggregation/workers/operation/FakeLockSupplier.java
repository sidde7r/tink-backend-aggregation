package se.tink.backend.aggregation.workers.operation;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.curator.framework.recipes.locks.InterProcessLock;

public class FakeLockSupplier implements LockSupplier {
    @Override
    public InterProcessLock getLock(String operationId) {
        return new InterProcessLock() {

            private final Lock lock = new ReentrantLock();

            @Override
            public void acquire() {
                lock.lock();
            }

            @Override
            public boolean acquire(long time, TimeUnit unit) throws Exception {
                return lock.tryLock(time, unit);
            }

            @Override
            public void release() {
                lock.unlock();
            }

            @Override
            public boolean isAcquiredInThisProcess() {
                boolean wasLocked = !lock.tryLock();
                if (!wasLocked) {
                    lock.unlock();
                }
                return wasLocked;
            }
        };
    }
}
