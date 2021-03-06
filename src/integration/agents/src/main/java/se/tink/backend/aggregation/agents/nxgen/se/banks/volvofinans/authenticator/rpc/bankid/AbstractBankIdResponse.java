package se.tink.backend.aggregation.agents.nxgen.se.banks.volvofinans.authenticator.rpc.bankid;

import com.google.common.base.Preconditions;
import lombok.Getter;
import se.tink.backend.aggregation.agents.bankid.status.BankIdStatus;
import se.tink.backend.aggregation.agents.nxgen.se.banks.volvofinans.VolvoFinansConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public abstract class AbstractBankIdResponse {

    @Getter private String status;

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
