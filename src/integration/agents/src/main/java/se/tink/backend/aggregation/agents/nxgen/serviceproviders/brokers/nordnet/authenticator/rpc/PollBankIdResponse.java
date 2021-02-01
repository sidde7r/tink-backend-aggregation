package se.tink.backend.aggregation.agents.nxgen.serviceproviders.brokers.nordnet.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import se.tink.backend.aggregation.agents.bankid.status.BankIdStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.brokers.nordnet.NordnetBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.brokers.nordnet.NordnetBaseConstants.Errors;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
public class PollBankIdResponse {
    private String status;

    @JsonProperty("hint_code")
    private String hintCode;

    @JsonProperty("error_code")
    private String errorCode;

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
