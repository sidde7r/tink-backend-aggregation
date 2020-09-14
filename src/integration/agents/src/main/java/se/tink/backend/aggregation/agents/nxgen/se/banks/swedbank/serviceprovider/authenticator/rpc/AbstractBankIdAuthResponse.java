package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.SwedbankBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.AbstractBankIdResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
public abstract class AbstractBankIdAuthResponse extends AbstractBankIdResponse {
    private String status;
    private String autoStartToken;

    @JsonIgnore
    public SwedbankBaseConstants.BankIdResponseStatus getBankIdStatus() {
        return SwedbankBaseConstants.BankIdResponseStatus.fromStatusCode(status);
    }

    public String getStatus() {
        return status;
    }
}
