package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.authenticator.entities.LinksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SignBasketResponse {

    private String scaStatus;

    private String authorisationId;

    @JsonProperty("_links")
    private LinksEntity links;

    public String getScaStatus() {
        return scaStatus;
    }

    public String getAuthorisationId() {
        return authorisationId;
    }

    public LinksEntity getLinks() {
        return links;
    }
}
