package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConfirmationResourceEntity {
    @JsonProperty("psuAuthenticationFactor")
    private String psuAuthenticationFactor = null;

    public String getPsuAuthenticationFactor() {
        return psuAuthenticationFactor;
    }

    public void setPsuAuthenticationFactor(String psuAuthenticationFactor) {
        this.psuAuthenticationFactor = psuAuthenticationFactor;
    }
}
