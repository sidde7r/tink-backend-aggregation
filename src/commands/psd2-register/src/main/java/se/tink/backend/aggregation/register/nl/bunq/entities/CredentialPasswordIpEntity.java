package se.tink.backend.aggregation.register.nl.bunq.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CredentialPasswordIpEntity {
    private int id;
    private String created;
    private String updated;
    private String status;

    @JsonProperty("expiry_time")
    private String expiryTime;

    @JsonProperty("token_value")
    private String tokenValue;

    @JsonProperty("permitted_device")
    private PermittedDeviceEntity permittedDevice;

    public String getTokenValue() {
        return tokenValue;
    }
}
