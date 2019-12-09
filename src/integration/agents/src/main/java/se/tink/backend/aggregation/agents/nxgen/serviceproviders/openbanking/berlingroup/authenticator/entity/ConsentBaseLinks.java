package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.entity;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConsentBaseLinks {
    private Href scaRedirect;
    private Href status;
    private Href scaStatus;

    public ConsentBaseLinks() {}

    public Href getScaRedirect() {
        return scaRedirect;
    }

    public Href getStatus() {
        return status;
    }

    public Href getScaStatus() {
        return scaStatus;
    }
}
