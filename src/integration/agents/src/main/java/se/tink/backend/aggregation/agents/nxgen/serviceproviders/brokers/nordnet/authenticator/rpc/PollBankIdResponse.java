package se.tink.backend.aggregation.agents.nxgen.serviceproviders.brokers.nordnet.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import se.tink.backend.aggregation.agents.bankid.status.BankIdStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.brokers.nordnet.NordnetBaseConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
public class PollBankIdResponse {
    private String status;

    @JsonProperty("hint_code")
    private String hintCode;

    public BankIdStatus getBankIdStatus() {
        switch (hintCode.toLowerCase()) {
            case NordnetBaseConstants.BankIdStatus.OUTSTANDING_TRANSACTION:
            case NordnetBaseConstants.BankIdStatus.USER_SIGN:
                return BankIdStatus.WAITING;
            case NordnetBaseConstants.BankIdStatus.COMPLETE:
                return BankIdStatus.DONE;
            case NordnetBaseConstants.BankIdStatus.NO_CLIENT:
                return BankIdStatus.NO_CLIENT;
            default:
                return BankIdStatus.FAILED_UNKNOWN;
        }
    }
}
