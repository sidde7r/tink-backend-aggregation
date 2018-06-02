package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v17.authenticator.entities.lightLogin;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.serializer.NordeaHashMapSerializer;

public class LightLoginRequestEntity {
    @JsonSerialize(using = NordeaHashMapSerializer.class)
    private String userId;
    @JsonSerialize(using = NordeaHashMapSerializer.class)
    private String password;
    @JsonSerialize(using = NordeaHashMapSerializer.class)
    private String type;
    @JsonSerialize(using = NordeaHashMapSerializer.class)
    private String deviceRegistrationToken;
    @JsonSerialize(using = NordeaHashMapSerializer.class)
    private String deviceId;

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setLightLoginType(String marketCode) {
        this.type = "lightLogin" + marketCode.toUpperCase();
    }

    public void setDeviceRegistrationToken(String deviceRegistrationToken) {
        this.deviceRegistrationToken = deviceRegistrationToken;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
}
