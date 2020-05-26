package se.tink.backend.aggregation.agents.nxgen.se.brokers.nordnet.authenticator.rpc;

import se.tink.backend.aggregation.agents.bankid.status.BankIdStatus;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.nordnet.NordnetConstants;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.nordnet.authenticator.rpc.entities.BankIdErrorEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BankIdCollectResponse {

    private String progressStatus;

    private String completeUrl;

    private BankIdErrorEntity error;

    public String getProgressStatus() {
        return progressStatus;
    }

    public String getCompleteUrl() {
        return completeUrl;
    }

    private String getStatus() {
        return (error != null) ? error.getCode() : progressStatus;
    }

    public BankIdStatus getBankIdStatus() {
        final NordnetConstants.BankIdResponseStatus status =
                NordnetConstants.BankIdResponseStatus.fromStatusCode(getStatus());

        switch (status) {
            case COMPLETE:
                return BankIdStatus.DONE;
            case USER_SIGN:
            case ALREADY_IN_PROGRESS:
                return BankIdStatus.WAITING;
            case NO_CLIENT:
                return BankIdStatus.NO_CLIENT;
            case CANCELLED:
                return BankIdStatus.CANCELLED;
            case TIMEOUT:
                return BankIdStatus.TIMEOUT;
            default:
                return BankIdStatus.FAILED_UNKNOWN;
        }
    }
}
