package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class EncryptedPayloadAndroidEntity {
    private String appType;
    private String appVersion;
    private String bankId;
    private String deviceId;
    private String deviceType;
    private String locale;
    private String nemidChallenge;
    private String nemidResponse;
    private String osVersion;
    private String pincode;
    private String screenSize;
    private String userId;

    public String getAppType() {
        return appType;
    }

    public void setAppType(String appType) {
        this.appType = appType;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public String getBankId() {
        return bankId;
    }

    public void setBankId(String bankId) {
        this.bankId = bankId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String getNemidChallenge() {
        return nemidChallenge;
    }

    public void setNemidChallenge(String nemidChallenge) {
        this.nemidChallenge = nemidChallenge;
    }

    public String getNemidResponse() {
        return nemidResponse;
    }

    public void setNemidResponse(String nemidResponse) {
        this.nemidResponse = nemidResponse;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
    }

    public String getPincode() {
        return pincode;
    }

    public void setPincode(String pincode) {
        this.pincode = pincode;
    }

    public String getScreenSize() {
        return screenSize;
    }

    public void setScreenSize(String screenSize) {
        this.screenSize = screenSize;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
