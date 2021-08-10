package se.tink.backend.aggregation.workers.operation;

import org.apache.curator.framework.recipes.locks.InterProcessLock;

@FunctionalInterface
public interface LockSupplier {
    InterProcessLock getLock(String operationId);
}
