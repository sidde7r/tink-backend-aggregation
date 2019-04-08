package se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordnet.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PrivateFeedEntity {
    private String hostname;
    private int port;
    private boolean encrypted;
}
