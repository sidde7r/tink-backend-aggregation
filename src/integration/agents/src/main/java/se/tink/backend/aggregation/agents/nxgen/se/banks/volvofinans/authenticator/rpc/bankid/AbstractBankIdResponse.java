package se.tink.backend.aggregation.agents.nxgen.se.banks.volvofinans.authenticator.rpc.bankid;

import com.google.common.base.Preconditions;
import se.tink.backend.aggregation.agents.BankIdStatus;
import se.tink.backend.aggregation.agents.nxgen.se.banks.volvofinans.VolvoFinansConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public abstract class AbstractBankIdResponse {

    private String status;

    public String getStatus() {
        return status;
    }

    public BankIdStatus getBankIdStatus() {
        Preconditions.checkState(status != null, "BankID status was null");
        switch (status.toUpperCase()) {
            case VolvoFinansConstants.BankIdStatus.DONE:
                return BankIdStatus.DONE;
            case VolvoFinansConstants.BankIdStatus.WAITING:
                return BankIdStatus.WAITING;
            default:
                return BankIdStatus.FAILED_UNKNOWN;
        }
    }
}
