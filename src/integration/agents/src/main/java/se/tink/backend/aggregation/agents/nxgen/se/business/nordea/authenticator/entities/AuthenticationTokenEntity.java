package se.tink.backend.aggregation.agents.nxgen.se.business.nordea.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AuthenticationTokenEntity {
    private String token;
    private String authLevel;
    private String loginTime;
    private String notAfter;
    private String sessionMaxLength;
    private String tokenMaxAge;
    private String profileId;

    public String getToken() {
        return token;
    }
}
