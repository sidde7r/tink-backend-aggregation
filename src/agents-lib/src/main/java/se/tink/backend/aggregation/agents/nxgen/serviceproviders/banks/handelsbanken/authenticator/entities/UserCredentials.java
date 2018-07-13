package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class UserCredentials {
    private String code;
    private String personalId;

    private UserCredentials() {
    }

    public static UserCredentials create(String userId, String code) {
        return new UserCredentials()
                .setCode(code)
                .setPersonalId(userId);
    }

    private UserCredentials setCode(String code) {
        this.code = code;
        return this;
    }

    private UserCredentials setPersonalId(String personalId) {
        this.personalId = personalId;
        return this;
    }
}
