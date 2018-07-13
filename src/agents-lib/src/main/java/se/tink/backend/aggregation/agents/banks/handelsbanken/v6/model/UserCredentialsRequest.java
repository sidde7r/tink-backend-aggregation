package se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model;

public class UserCredentialsRequest {
    private UserCredentialsEntity userCredentials;

    public UserCredentialsRequest(String personalId, String code) {
        userCredentials = new UserCredentialsEntity(personalId, code);
    }

    public UserCredentialsEntity getUserCredentials() {
        return userCredentials;
    }

    public void setUserCredentials(UserCredentialsEntity userCredentials) {
        this.userCredentials = userCredentials;
    }
}
