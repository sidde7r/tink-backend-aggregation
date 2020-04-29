package se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.authenticator.entity;

import se.tink.backend.aggregation.agents.Href;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LinksEntity {

    private Href scaStatus;

    private Href scaRedirect;

    private Href status;

    public Href getScaStatus() {
        return scaStatus;
    }

    public Href getHrefEntity() {
        return scaRedirect;
    }

    public Href getStatus() {
        return status;
    }
}
