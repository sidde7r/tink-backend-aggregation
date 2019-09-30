package se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.authenticator.entities;

import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.CommerzbankConstants.Values;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AutoLoginEntity {
    private boolean createSessionToken = Values.CREATE_SESSION_TOKEN_TRUE;
    private String biometricType = Values.BIOMETRIC_TOUCHID;
    private String appId;
    private String pin;
    private String userid;

    private AutoLoginEntity(String userId, String pin, String appId) {
        this.userid = userId;
        this.pin = pin;
        this.appId = appId;
    }

    public static AutoLoginEntity create(String userId, String pin, String appId) {
        return new AutoLoginEntity(userId, pin, appId);
    }
}
