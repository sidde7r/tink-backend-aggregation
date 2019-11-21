package se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.authenticator.entities;

import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.HandelsbankenNOConstants.LogInRequestConstants;
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
        return new CredentialsEntity(evryToken, LogInRequestConstants.TOKEN_TYPE);
    }
}
