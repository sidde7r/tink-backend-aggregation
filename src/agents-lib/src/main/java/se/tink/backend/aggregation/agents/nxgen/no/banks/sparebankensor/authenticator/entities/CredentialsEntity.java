package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CredentialsEntity {
    private String token;
    private String tokenType;

    private CredentialsEntity(String token, String tokenType) {
        this.token = token;
        this.tokenType = tokenType;
    }

    public static CredentialsEntity build(String evryToken) {
        // The front slashes in the evryToken have to be escaped with back slashes
        String escapedEvryToken = evryToken.replaceAll("/", "\\\\/");
        return new CredentialsEntity(escapedEvryToken, "EvrySO");
    }
}
