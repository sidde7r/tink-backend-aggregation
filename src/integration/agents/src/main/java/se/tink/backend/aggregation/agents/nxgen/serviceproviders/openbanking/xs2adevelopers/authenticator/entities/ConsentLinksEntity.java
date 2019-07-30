package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersConstants.Transactions;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConsentLinksEntity {

    @JsonProperty("scaOAuth")
    private JsonNode scaOAuthInternal;

    @JsonProperty("scaStatus")
    private JsonNode scaStatusInternal;

    @JsonProperty("self")
    private JsonNode selfInternal;

    @JsonProperty("status")
    private JsonNode statusInternal;

    private String scaOAuth;
    private String scaStatus;
    private String self;
    private String status;

    public String getScaOAuth() {
        if (scaOAuth == null && scaOAuthInternal != null) {
            return getUrlFromNode(scaOAuthInternal);
        }
        return scaOAuth;
    }

    public String getScaStatus() {
        if (scaStatus == null && scaStatusInternal != null) {
            return getUrlFromNode(scaStatusInternal);
        }
        return scaStatus;
    }

    public String getSelf() {
        if (self == null && selfInternal != null) {
            return getUrlFromNode(selfInternal);
        }
        return self;
    }

    public String getStatus() {
        if (status == null && statusInternal != null) {
            return getUrlFromNode(statusInternal);
        }
        return status;
    }

    private String getUrlFromNode(JsonNode node) {
        if (node.isObject()) {
            return node.get(Transactions.HREF).asText();
        } else if (node.isTextual()) {
            return node.asText();
        } else {
            throw new IllegalStateException(ErrorMessages.PARSING_URL);
        }
    }
}
