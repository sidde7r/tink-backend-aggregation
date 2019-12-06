package se.tink.backend.aggregation.agents.nxgen.de.openbanking.santander.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConsentLinks {

    private String scaOAuth;
    private String self;
    private String status;

    public ConsentLinks() {}

    public String getScaOAuth() {
        return scaOAuth;
    }

    public String getSelf() {
        return self;
    }

    public String getStatus() {
        return status;
    }
}
