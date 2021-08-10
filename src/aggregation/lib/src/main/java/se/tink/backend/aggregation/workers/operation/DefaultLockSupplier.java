package se.tink.backend.aggregation.workers.operation;

import com.google.inject.Inject;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessLock;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;

public class DefaultLockSupplier implements LockSupplier {

    private final CuratorFramework curatorFramework;

    @Inject
    public DefaultLockSupplier(CuratorFramework curatorFramework) {
        this.curatorFramework = curatorFramework;
    }

    @Override
    public InterProcessLock getLock(String path) {
        return new InterProcessMutex(curatorFramework, path);
    }
}
