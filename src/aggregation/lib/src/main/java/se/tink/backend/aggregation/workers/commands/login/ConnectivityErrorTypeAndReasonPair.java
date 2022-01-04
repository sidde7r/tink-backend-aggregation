package se.tink.backend.aggregation.workers.commands.login;

import lombok.EqualsAndHashCode;
import se.tink.backend.aggregation.agents.exceptions.connectivity.ConnectivityException;
import se.tink.connectivity.errors.ConnectivityErrorType;

@EqualsAndHashCode
class ConnectivityErrorTypeAndReasonPair {

    private ConnectivityErrorType type;
    private String reason;

    ConnectivityErrorTypeAndReasonPair(ConnectivityException connectException) {
        type = connectException.getError().getType();
        reason = connectException.getError().getDetails().getReason();
    }

    ConnectivityErrorTypeAndReasonPair(ConnectivityErrorType type, String reason) {
        this.type = type;
        this.reason = reason;
    }
}
