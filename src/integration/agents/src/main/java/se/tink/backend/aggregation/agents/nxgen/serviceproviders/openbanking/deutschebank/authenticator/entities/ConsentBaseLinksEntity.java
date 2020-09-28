package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.authenticator.entities;

import se.tink.backend.aggregation.agents.Href;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConsentBaseLinksEntity implements AccessEntity {
    private Href scaRedirect;
    private Href status;
    private Href scaStatus;
    private Href self;

    public Href getScaRedirect() {
        return scaRedirect;
    }

    public Href getStatus() {
        return status;
    }

    public Href getScaStatus() {
        return scaStatus;
    }

    public Href getSelf() {
        return self;
    }
}
