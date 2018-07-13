package se.tink.backend.aggregation.agents.banks.nordea.v14.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LoginResponse {
    protected LightLoginResponse lightLoginResponse;

    public LightLoginResponse getLightLoginResponse() {
        return lightLoginResponse;
    }

    public void setLightLoginResponse(LightLoginResponse lightLoginResponse) {
        this.lightLoginResponse = lightLoginResponse;
    }
}
