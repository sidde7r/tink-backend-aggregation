package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole.payment.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class HalEndUserIdentityEntity {
    @JsonProperty("connectedPsu")
    private String connectedPsu = null;

    @JsonProperty("_links")
    private EndUserIdentityLinksEntity links = null;

    public String getConnectedPsu() {
        return connectedPsu;
    }

    public void setConnectedPsu(String connectedPsu) {
        this.connectedPsu = connectedPsu;
    }

    public EndUserIdentityLinksEntity getLinks() {
        return links;
    }

    public void setLinks(EndUserIdentityLinksEntity links) {
        this.links = links;
    }
}
