package se.tink.backend.aggregation.agents.nxgen.serviceproviders.brokers.nordnet.authenticator.rpc;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import se.tink.backend.aggregation.agents.bankid.status.BankIdStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.brokers.nordnet.NordnetBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.brokers.nordnet.NordnetBaseConstants.Errors;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class PollBankIdResponse {
    private String status;
    private String hintCode;
    private String errorCode;
    private boolean loggedIn;
    private String sessionKey;
    private String sessionType;

    public BankIdStatus getBankIdStatus() {
        if (status.equalsIgnoreCase(Errors.ERROR)
                && NordnetBaseConstants.BankIdStatus.ALREADY_IN_PROGRESS.equalsIgnoreCase(
                        errorCode)) {
            return BankIdStatus.INTERRUPTED;
        }

        switch (hintCode.toLowerCase()) {
            case NordnetBaseConstants.BankIdStatus.STARTED:
            case NordnetBaseConstants.BankIdStatus.OUTSTANDING_TRANSACTION:
            case NordnetBaseConstants.BankIdStatus.NO_CLIENT:
            case NordnetBaseConstants.BankIdStatus.USER_SIGN:
                return BankIdStatus.WAITING;
            case NordnetBaseConstants.BankIdStatus.COMPLETE:
                return BankIdStatus.DONE;
            default:
                return BankIdStatus.FAILED_UNKNOWN;
        }
    }
}
