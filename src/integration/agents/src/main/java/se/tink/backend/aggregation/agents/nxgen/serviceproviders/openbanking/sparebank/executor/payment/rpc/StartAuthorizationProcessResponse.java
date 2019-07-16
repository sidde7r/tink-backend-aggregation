package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.executor.payment.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.executor.payment.entities.LinksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class StartAuthorizationProcessResponse {
    private String scaStatus;

    @JsonProperty("_links")
    private LinksEntity links;

    public boolean hasScaRedirectLink() {
        return links != null && links.hasScaRedirectEntity();
    }

    public String getScaRedirectLink() {
        return links.getScaRedirectEntity();
    }
}
