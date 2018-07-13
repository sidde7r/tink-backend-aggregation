package se.tink.backend.aggregation.agents.banks.nordea.v14.model;

public class LoginRequest {
    protected LightLoginRequest lightLoginRequest;

    public LightLoginRequest getLightLoginRequest() {
        return lightLoginRequest;
    }

    public void setLightLoginRequest(LightLoginRequest lightLoginRequest) {
        this.lightLoginRequest = lightLoginRequest;
    }
}
