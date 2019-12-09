package se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConsentResponse {

    private String consentId;

    @JsonProperty("_links")
    private Links links;

    private String consentStatus;

    private String statementStatus;

    public String getConsentId() {
        return consentId;
    }

    public Links getLinks() {
        return links;
    }

    public String getConsentStatus() {
        return consentStatus;
    }

    public String getStatementStatus() {
        return statementStatus;
    }
}
