package se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.authenticator.entity;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AuthTokenEntity {
    private String expiresIn;
    private String token;

    public String getToken() {
        return token;
    }
}
