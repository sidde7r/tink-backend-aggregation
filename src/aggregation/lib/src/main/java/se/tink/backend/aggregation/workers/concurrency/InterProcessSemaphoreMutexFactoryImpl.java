package se.tink.backend.aggregation.workers.concurrency;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessLock;
import org.apache.curator.framework.recipes.locks.InterProcessSemaphoreMutex;

public class InterProcessSemaphoreMutexFactoryImpl implements InterProcessSemaphoreMutexFactory {

    public InterProcessLock createLock(CuratorFramework curatorFramework, String name) {
        return new InterProcessSemaphoreMutex(curatorFramework, name);
    }
}
