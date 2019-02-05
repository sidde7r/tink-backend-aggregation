package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.SwedbankBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.AbstractBankIdResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public abstract class AbstractBankIdAuthResponse extends AbstractBankIdResponse {
    private String status;

    @JsonIgnore
    public SwedbankBaseConstants.BankIdResponseStatus getBankIdStatus() {
        return SwedbankBaseConstants.BankIdResponseStatus.fromStatusCode(status);
    }

    public String getStatus() {
        return status;
    }
}
