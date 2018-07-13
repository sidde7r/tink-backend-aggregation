package se.tink.backend.aggregation.agents.banks.nordea.v18.model;

public class LoginRequest {
    private LightLoginRequest lightLoginRequest;

    public LightLoginRequest getLightLoginRequest() {
        return lightLoginRequest;
    }

    public void setLightLoginRequest(LightLoginRequest lightLoginRequest) {
        this.lightLoginRequest = lightLoginRequest;
    }
}
