package se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model;

public class ActivateProfileResponse extends AbstractResponse {
    private String profileId;

    public String getProfileId() {
        return profileId;
    }

    public void setProfileId(String profileId) {
        this.profileId = profileId;
    }
}
