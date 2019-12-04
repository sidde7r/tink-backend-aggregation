package se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.authentication;

public class BancoBpiUserState {

    private String accessPin;
    private String deviceUUID;
    private String sessionCSRFToken;
    private boolean deviceActivationFinished;
    private MobileChallengeRequestedToken mobileChallengeRequestedToken;

    public String getAccessPin() {
        return accessPin;
    }

    public void setAccessPin(String accessPin) {
        this.accessPin = accessPin;
    }

    public String getDeviceUUID() {
        return deviceUUID;
    }

    public void setDeviceUUID(String deviceUUID) {
        this.deviceUUID = deviceUUID;
    }

    public String getSessionCSRFToken() {
        return sessionCSRFToken;
    }

    public void setSessionCSRFToken(String sessionCSRFToken) {
        this.sessionCSRFToken = sessionCSRFToken;
    }

    public MobileChallengeRequestedToken getMobileChallengeRequestedToken() {
        return mobileChallengeRequestedToken;
    }

    public void setMobileChallengeRequestedToken(
            MobileChallengeRequestedToken mobileChallengeRequestedToken) {
        this.mobileChallengeRequestedToken = mobileChallengeRequestedToken;
    }

    void clearAuthData() {
        accessPin = null;
        deviceUUID = null;
        mobileChallengeRequestedToken = null;
        sessionCSRFToken = null;
        deviceActivationFinished = false;
    }

    public boolean isDeviceActivationFinished() {
        return deviceActivationFinished;
    }

    public void finishDeviceActivation() {
        this.deviceActivationFinished = true;
        mobileChallengeRequestedToken = null;
    }
}
