package se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model;

public class ServerProfileRequest {
    private String encUserCredentials;
    private String profileId;
    public String getEncUserCredentials() {
        return encUserCredentials;
    }
    public void setEncUserCredentials(String encUserCredentials) {
        this.encUserCredentials = encUserCredentials;
    }

    public String getProfileId() {
        return profileId;
    }
    public void setProfileId(String profileId) {
        this.profileId = profileId;
    }
    

}
