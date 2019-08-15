package se.tink.backend.aggregation.agents.nxgen.de.openbanking.targobank.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.targobank.authenticator.entities.PasswordAuthenticationLinksEntity;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.targobank.authenticator.entities.ScaMethodEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PasswordAuthenticationResponse {
    private List<ScaMethodEntity> scaMethods;
    private String scaStatus;

    @JsonProperty("_links")
    private PasswordAuthenticationLinksEntity links;

    public PasswordAuthenticationLinksEntity getLinks() {
        return links;
    }

    public List<ScaMethodEntity> getScaMethods() {
        return scaMethods;
    }
}
