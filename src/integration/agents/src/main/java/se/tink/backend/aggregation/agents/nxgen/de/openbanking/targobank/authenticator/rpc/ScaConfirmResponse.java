package se.tink.backend.aggregation.agents.nxgen.de.openbanking.targobank.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.targobank.authenticator.entities.ScaConfirmLinksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ScaConfirmResponse {
    @JsonProperty("_links")
    private ScaConfirmLinksEntity links;

    private String scaStatus;

    public String getScaStatus() {
        return scaStatus;
    }
}
