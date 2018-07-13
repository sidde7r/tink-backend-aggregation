package se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model;

public class CreateProfileResponse extends AbstractResponse {
    private String pdeviceServerProfile;
    private String challenge;

    public String getChallenge() {
        return challenge;
    }

    public void setChallenge(String challenge) {
        this.challenge = challenge;
    }

    public String getPdeviceServerProfile() {
        return pdeviceServerProfile;
    }

    public void setPdeviceServerProfile(String pdeviceServerProfile) {
        this.pdeviceServerProfile = pdeviceServerProfile;
    }
}
