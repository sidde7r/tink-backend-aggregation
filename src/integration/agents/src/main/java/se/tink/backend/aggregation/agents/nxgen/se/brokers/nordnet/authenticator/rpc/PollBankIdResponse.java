package se.tink.backend.aggregation.agents.nxgen.se.brokers.nordnet.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import se.tink.backend.aggregation.agents.bankid.status.BankIdStatus;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.nordnet.NordnetConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
public class PollBankIdResponse {
    private String status;

    @JsonProperty("hint_code")
    private String hintCode;

    public BankIdStatus getBankIdStatus() {
        switch (hintCode.toLowerCase()) {
            case NordnetConstants.BankIdStatus.OUTSTANDING_TRANSACTION:
            case NordnetConstants.BankIdStatus.USER_SIGN:
                return BankIdStatus.WAITING;
            case NordnetConstants.BankIdStatus.COMPLETE:
                return BankIdStatus.DONE;
            case NordnetConstants.BankIdStatus.NO_CLIENT:
                return BankIdStatus.NO_CLIENT;
            default:
                return BankIdStatus.FAILED_UNKNOWN;
        }
    }
}
