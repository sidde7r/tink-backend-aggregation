package se.tink.backend.aggregation.workers.operation;

import com.google.common.base.Objects;

public enum RequestStatus {
    STARTED(1),
    TRYING_TO_ABORT(2),
    ABORTING(3),
    IMPOSSIBLE_TO_ABORT(4),
    ABORTED(5),
    COMPLETED(6);

    private final int intValue;

    RequestStatus(int intValue) {
        this.intValue = intValue;
    }

    public int getIntValue() {
        return intValue;
    }

    public static RequestStatus getStatus(int status) {
        for (RequestStatus requestStatus : values()) {
            if (Objects.equal(requestStatus.getIntValue(), status)) {
                return requestStatus;
            }
        }

        throw new IllegalStateException(
                String.format("Could not convert integer to enum, integer is %d", status));
    }
}
