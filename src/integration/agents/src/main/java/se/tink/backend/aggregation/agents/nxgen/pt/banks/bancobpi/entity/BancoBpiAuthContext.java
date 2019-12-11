package se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.entity;

public class BancoBpiAuthContext {

    private String accessPin;
    private String deviceUUID;
    private String sessionCSRFToken;
    private String moduleVersion;
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

    public String getModuleVersion() {
        return moduleVersion;
    }

    public void setModuleVersion(String moduleVersion) {
        this.moduleVersion = moduleVersion;
    }

    public void setDeviceActivationFinished(boolean deviceActivationFinished) {
        this.deviceActivationFinished = deviceActivationFinished;
    }

    public MobileChallengeRequestedToken getMobileChallengeRequestedToken() {
        return mobileChallengeRequestedToken;
    }

    public void setMobileChallengeRequestedToken(
            MobileChallengeRequestedToken mobileChallengeRequestedToken) {
        this.mobileChallengeRequestedToken = mobileChallengeRequestedToken;
    }

    public void clearAuthData() {
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
