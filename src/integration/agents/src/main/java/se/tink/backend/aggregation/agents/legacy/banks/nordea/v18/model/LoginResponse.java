package se.tink.backend.aggregation.agents.banks.nordea.v18.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LoginResponse {
    private LightLoginResponse lightLoginResponse;

    public LightLoginResponse getLightLoginResponse() {
        return lightLoginResponse;
    }

    public void setLightLoginResponse(LightLoginResponse lightLoginResponse) {
        this.lightLoginResponse = lightLoginResponse;
    }
}
