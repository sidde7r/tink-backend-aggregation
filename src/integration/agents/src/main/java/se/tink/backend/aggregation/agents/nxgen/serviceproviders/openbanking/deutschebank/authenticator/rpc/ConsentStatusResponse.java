package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheBankConstants.StatusValues;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConsentStatusResponse {
    @JsonProperty private String consentStatus;

    @JsonIgnore
    public String getConsentStatus() {
        return consentStatus;
    }

    @JsonIgnore
    public boolean isValid() {
        return StatusValues.VALID.equalsIgnoreCase(consentStatus);
    }
}
