package se.tink.backend.aggregation.workers.operation;

public enum OperationStatus {
    STARTED,
    TRYING_TO_ABORT,
    ABORTING,
    ABORTED,
    IMPOSSIBLE_TO_ABORT,
}
