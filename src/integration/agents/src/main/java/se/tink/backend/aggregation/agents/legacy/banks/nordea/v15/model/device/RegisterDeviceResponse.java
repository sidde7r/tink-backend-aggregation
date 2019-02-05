package se.tink.backend.aggregation.agents.banks.nordea.v15.model.device;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RegisterDeviceResponse {
    @JsonProperty("registerDeviceResponse")
    private RegisterDeviceResponseData data;

    public RegisterDeviceResponseData getData() {
        return data;
    }

    public void setData(RegisterDeviceResponseData data) {
        this.data = data;
    }

}
