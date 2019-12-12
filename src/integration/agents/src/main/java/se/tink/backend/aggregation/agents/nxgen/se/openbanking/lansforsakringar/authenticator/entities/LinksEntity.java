package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.authenticator.entities;

import se.tink.backend.aggregation.agents.Href;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LinksEntity {

    private Href scaStatus;

    private Href scaOAuth;

    private Href self;

    private Href startAuthorisation;

    private Href status;

    public Href getScaStatus() {
        return scaStatus;
    }

    public Href getScaOAuth() {
        return scaOAuth;
    }

    public Href getSelf() {
        return self;
    }

    public Href getStartAuthorisation() {
        return startAuthorisation;
    }

    public Href getStatus() {
        return status;
    }
}
