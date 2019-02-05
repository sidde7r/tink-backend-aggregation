package se.tink.backend.aggregation.agents.banks.nordea.v20.model.authentication;

import com.fasterxml.jackson.annotation.JsonProperty;

public class StrongLoginRequest {

    @JsonProperty("strongLoginRequest")
    private StrongLoginRequestIn strongLoginRequestIn;

    public StrongLoginRequest(String userId, String password) {
        strongLoginRequestIn = new StrongLoginRequestIn();
        strongLoginRequestIn.setType("stepUpFI");
        strongLoginRequestIn.setPassword(password);
        strongLoginRequestIn.setUserId(userId);
    }

    public StrongLoginRequestIn getStrongLoginRequestIn() {
        return strongLoginRequestIn;
    }

    public void setStrongLoginRequestIn(StrongLoginRequestIn strongLoginRequestIn) {
        this.strongLoginRequestIn = strongLoginRequestIn;
    }
}
