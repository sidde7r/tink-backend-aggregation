package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.password.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BindDeviceResponse extends AbstractDeviceBindResponse {
    @JsonProperty("DeviceSerialNo")
    private String deviceSerialNumber;

    @JsonProperty("SharedSecret")
    private String sharedSecret;

    public String getDeviceSerialNumber() {
        return deviceSerialNumber;
    }

    public String getSharedSecret() {
        return sharedSecret;
    }
}
