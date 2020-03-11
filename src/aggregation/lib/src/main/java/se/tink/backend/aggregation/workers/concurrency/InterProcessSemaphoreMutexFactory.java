package se.tink.backend.aggregation.workers.concurrency;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessLock;

public interface InterProcessSemaphoreMutexFactory {

    InterProcessLock createLock(CuratorFramework curatorFramework, String name);
}
