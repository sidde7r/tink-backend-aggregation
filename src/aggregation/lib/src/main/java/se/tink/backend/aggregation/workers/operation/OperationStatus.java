package se.tink.backend.aggregation.workers.operation;

import com.google.common.base.Objects;

public enum OperationStatus {
    STARTED(1),
    TRYING_TO_ABORT(2),
    ABORTING(3),
    IMPOSSIBLE_TO_ABORT(4),
    ABORTED(5),
    COMPLETED_WITHOUT_ABORT(6);

    private final int status;

    OperationStatus(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }

    public static OperationStatus getStatus(int status) {
        for (OperationStatus operationStatus : values()) {
            if (Objects.equal(operationStatus.getStatus(), status)) {
                return operationStatus;
            }
        }

        throw new IllegalStateException(
                String.format("Could not convert integer to enum, integer is %d", status));
    }
}
