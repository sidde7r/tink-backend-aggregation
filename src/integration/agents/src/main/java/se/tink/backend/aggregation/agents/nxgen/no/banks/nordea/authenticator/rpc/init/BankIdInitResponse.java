package se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.authenticator.rpc.init;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Objects;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.NordeaNoConstants.BankIdStatus;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.authenticator.entities.init.PerformBankIDSIMInitOutEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v17.rpc.NordeaResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BankIdInitResponse extends NordeaResponse {
    private PerformBankIDSIMInitOutEntity performBankIDSIMInitOut;

    public PerformBankIDSIMInitOutEntity getPerformBankIDSIMInitOut() {
        return performBankIDSIMInitOut;
    }

    public String getSessionId() {
        return performBankIDSIMInitOut.getSessionId();
    }

    @JsonIgnore
    public boolean isAlreadyInProgress() {
        return getErrorCode().isPresent()
                && Objects.equals(getErrorCode().get(), BankIdStatus.ALREADY_IN_PROGRESS);
    }

    @JsonIgnore
    public String getMerchantReference() {
        return performBankIDSIMInitOut.getMerchantReference();
    }

    @JsonIgnore
    public String getToken() {
        return performBankIDSIMInitOut.getAuthenticationToken().getToken();
    }
}
