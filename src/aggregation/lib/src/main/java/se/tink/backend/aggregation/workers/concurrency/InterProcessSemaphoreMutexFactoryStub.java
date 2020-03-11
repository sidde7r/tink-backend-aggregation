package se.tink.backend.aggregation.workers.concurrency;

import java.util.concurrent.TimeUnit;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessLock;

public class InterProcessSemaphoreMutexFactoryStub implements InterProcessSemaphoreMutexFactory {

    public class StubInterProcessLock implements InterProcessLock {

        private boolean acquired = false;

        public void acquire() throws Exception {
            acquired = true;
        }

        public boolean acquire(long var1, TimeUnit var3) throws Exception {
            return true;
        }

        public void release() throws Exception {
            acquired = false;
        }

        public boolean isAcquiredInThisProcess() {
            return acquired;
        }
    }

    public InterProcessLock createLock(CuratorFramework curatorFramework, String name) {
        return new StubInterProcessLock();
    }
}
