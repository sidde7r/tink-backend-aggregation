package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.entity;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConsentBaseWithoutLinks {
    private String scaRedirect;
    private String status;
    private String scaStatus;

    public ConsentBaseWithoutLinks() {}

    public String getScaRedirect() {
        return scaRedirect;
    }

    public String getStatus() {
        return status;
    }

    public String getScaStatus() {
        return scaStatus;
    }
}
