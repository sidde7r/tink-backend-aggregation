package se.tink.backend.aggregation.agents.banks.nordea.v15.model.device;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RegisterDeviceRequest {
    @JsonProperty("registerDeviceRequest")
    private RegisterDeviceRequestData data;

    public RegisterDeviceRequestData getData() {
        return data;
    }

    public void setData(RegisterDeviceRequestData data) {
        this.data = data;
    }

}
