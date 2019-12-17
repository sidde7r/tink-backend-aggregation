package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.authenticator.entities;

import se.tink.backend.aggregation.agents.Href;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LinksEntity {

    private Href scaStatus;

    private Href scaRedirect;

    private Href self;

    private Href status;

    public Href getScaStatus() {
        return scaStatus;
    }

    public Href getScaRedirect() {
        return scaRedirect;
    }

    public Href getSelf() {
        return self;
    }

    public Href getStatus() {
        return status;
    }
}
