package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Arrays;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class RegisterDeviceRequest {
    @JsonProperty("description")
    private final String deviceName;

    @JsonProperty("secret")
    private final String apiKey;

    @JsonProperty("permitted_ips")
    private List<String> permittedIps;

    private RegisterDeviceRequest(String deviceName, String apiKey) {
        this.deviceName = deviceName;
        this.apiKey = apiKey;
    }

    public RegisterDeviceRequest(String deviceName, String apiKey, List<String> permittedIps) {
        this.deviceName = deviceName;
        this.apiKey = apiKey;
        this.permittedIps = permittedIps;
    }

    public static RegisterDeviceRequest createFromApiKey(String deviceName, String apiKey) {
        return new RegisterDeviceRequest(deviceName, apiKey);
    }

    public static RegisterDeviceRequest createFromApiKeyAllIPs(String deviceName, String apiKey) {
        return new RegisterDeviceRequest(deviceName, apiKey, Arrays.asList("*"));
    }

    public String getApiKey() {
        return apiKey;
    }
}
