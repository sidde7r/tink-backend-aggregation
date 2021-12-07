package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.norwegian.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConsentResponse {
    private String consentStatus;
    private String consentId;

    @JsonProperty("_links")
    private LinksEntity links;

    public String getRedirectUrl() {
        return links.scaRedirect.href;
    }

    public String getConsentId() {
        return consentId;
    }

    @JsonObject
    private static class LinksEntity {
        private ScaRedirectEntity scaRedirect;
    }

    @JsonObject
    private static class ScaRedirectEntity {
        private String href;
    }
}
