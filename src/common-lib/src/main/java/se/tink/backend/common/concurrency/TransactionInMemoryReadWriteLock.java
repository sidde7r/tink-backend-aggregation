package se.tink.backend.common.concurrency;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.framework.recipes.locks.InterProcessReadWriteLock;

public class TransactionInMemoryReadWriteLock {

    private static final String LOCK_PATH_PREFIX = "/locks/transactionsInMemory/";
    private final InterProcessReadWriteLock readWriteLock;

    public TransactionInMemoryReadWriteLock(CuratorFramework coordinationClient, String userId) {
        this.readWriteLock = new InterProcessReadWriteLock(coordinationClient, LOCK_PATH_PREFIX + userId);
    }

    public InterProcessMutex getLockForHoldingTransactionsInMemory() {
        return readWriteLock.readLock();
    }

    public InterProcessMutex getLockForModifyingTransactionsInDatabase() {
        return readWriteLock.writeLock();
    }
}
