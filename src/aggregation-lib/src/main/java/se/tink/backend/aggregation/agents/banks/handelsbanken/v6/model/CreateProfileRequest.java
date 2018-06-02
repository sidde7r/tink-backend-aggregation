package se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model;

public class CreateProfileRequest {
    private String encUserCredentials;

    public CreateProfileRequest(String encUserCredentials) {
        this.encUserCredentials = encUserCredentials;
    }

    public String getEncUserCredentials() {
        return encUserCredentials;
    }

    public void setEncUserCredentials(String encUserCredentials) {
        this.encUserCredentials = encUserCredentials;
    }

}
