package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.authenticator.entities.SecurityKeyEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class RegisterDeviceRequest {
    @JsonProperty("security_key")
    private SecurityKeyEntity securityKey = new SecurityKeyEntity();
    @JsonProperty("device_id")
    private String deviceId;

    public RegisterDeviceRequest setCodeCardValue(String codeCardValue) {
        securityKey.setValue(codeCardValue);
        return this;
    }

    public RegisterDeviceRequest setDeviceId(String deviceId) {
        this.deviceId = deviceId;
        return this;
    }
}
