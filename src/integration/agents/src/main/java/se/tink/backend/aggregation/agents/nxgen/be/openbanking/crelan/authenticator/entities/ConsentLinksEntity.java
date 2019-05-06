package se.tink.backend.aggregation.agents.nxgen.be.openbanking.crelan.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConsentLinksEntity {

    private String scaOAuth;
    private String scaStatus;
    private String self;
    private String status;
}
