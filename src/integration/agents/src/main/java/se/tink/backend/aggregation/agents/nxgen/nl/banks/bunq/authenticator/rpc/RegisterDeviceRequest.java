package se.tink.backend.aggregation.agents.nxgen.nl.banks.bunq.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class RegisterDeviceRequest {
    @JsonProperty("description")
    private final String deviceName;

    @JsonProperty("secret")
    private final String apiKey;

    private RegisterDeviceRequest(String deviceName, String apiKey) {
        this.deviceName = deviceName;
        this.apiKey = apiKey;
    }

    public static RegisterDeviceRequest createFromApiKey(String deviceName, String apiKey) {
        return new RegisterDeviceRequest(deviceName, apiKey);
    }

    public String getDeviceName() {
        return deviceName;
    }

    public String getApiKey() {
        return apiKey;
    }
}
