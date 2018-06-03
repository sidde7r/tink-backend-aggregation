package se.tink.backend.common.concurrency;

import javax.inject.Inject;
import org.apache.curator.framework.CuratorFramework;

public class LockFactory {
    private final CuratorFramework coordinationClient;

    @Inject
    public LockFactory(CuratorFramework coordinationClient) {
        this.coordinationClient = coordinationClient;
    }
}
