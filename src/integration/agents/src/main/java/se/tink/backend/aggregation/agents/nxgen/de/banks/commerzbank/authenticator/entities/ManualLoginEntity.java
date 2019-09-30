package se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.authenticator.entities;

import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.CommerzbankConstants.Values;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ManualLoginEntity {
    private boolean createSessionToken = Values.CREATE_SESSION_TOKEN_FALSE;
    private String pin;
    private String userid;

    private ManualLoginEntity(String userId, String pin) {
        this.userid = userId;
        this.pin = pin;
    }

    public static ManualLoginEntity create(String userId, String pin) {
        return new ManualLoginEntity(userId, pin);
    }
}
