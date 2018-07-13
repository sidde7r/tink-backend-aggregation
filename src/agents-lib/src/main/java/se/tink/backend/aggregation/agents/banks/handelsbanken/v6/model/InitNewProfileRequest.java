package se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model;

public class InitNewProfileRequest {
    private String appId;
    private String authTp;
    private String cnonce;
    private String deviceSecurityContextId;
    private String handshakeKey;
    private String profileTransformationTp;

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getAuthTp() {
        return authTp;
    }

    public void setAuthTp(String authTp) {
        this.authTp = authTp;
    }

    public String getCnonce() {
        return cnonce;
    }

    public void setCnonce(String cnonce) {
        this.cnonce = cnonce;
    }

    public String getDeviceSecurityContextId() {
        return deviceSecurityContextId;
    }

    public void setDeviceSecurityContextId(String deviceSecurityContextId) {
        this.deviceSecurityContextId = deviceSecurityContextId;
    }

    public String getHandshakeKey() {
        return handshakeKey;
    }

    public void setHandshakeKey(String handshakeKey) {
        this.handshakeKey = handshakeKey;
    }

    public String getProfileTransformationTp() {
        return profileTransformationTp;
    }

    public void setProfileTransformationTp(String profileTransformationTp) {
        this.profileTransformationTp = profileTransformationTp;
    }

}
