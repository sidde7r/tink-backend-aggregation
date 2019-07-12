package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConsentLinksEntity {

    private String scaOAuth;
    private String scaStatus;
    private String self;
    private String status;

    public String getScaOAuth() {
        return scaOAuth;
    }

    public String getScaStatus() {
        return scaStatus;
    }

    public String getSelf() {
        return self;
    }

    public String getStatus() {
        return status;
    }
}
