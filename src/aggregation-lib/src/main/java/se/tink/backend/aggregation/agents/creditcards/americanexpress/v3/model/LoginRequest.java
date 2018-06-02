package se.tink.backend.aggregation.agents.creditcards.americanexpress.v3.model;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoginRequest {
    private String timeZoneOffsetInMilli;
    private String clientVersion;
    private String hardwareId;
    private String osBuild;
    private String password;
    private String user;
    private String deviceModel;
    private String locale;
    private String rememberMe;
    private String clientType;
    private String userTimeStampInMilli;
    private String profileInfo;

    public String getTimeZoneOffsetInMilli() {
        return timeZoneOffsetInMilli;
    }

    public void setTimeZoneOffsetInMilli(String timeZoneOffsetInMilli) {
        this.timeZoneOffsetInMilli = timeZoneOffsetInMilli;
    }

    public String getClientVersion() {
        return clientVersion;
    }

    public void setClientVersion(String clientVersion) {
        this.clientVersion = clientVersion;
    }

    public String getHardwareId() {
        return hardwareId;
    }

    public void setHardwareId(String hardwareId) {
        this.hardwareId = hardwareId;
    }

    public String getOsBuild() {
        return osBuild;
    }

    public void setOsBuild(String osBuild) {
        this.osBuild = osBuild;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getDeviceModel() {
        return deviceModel;
    }

    public void setDeviceModel(String deviceModel) {
        this.deviceModel = deviceModel;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String getRememberMe() {
        return rememberMe;
    }

    public void setRememberMe(String rememberMe) {
        this.rememberMe = rememberMe;
    }

    public String getClientType() {
        return clientType;
    }

    public void setClientType(String clientType) {
        this.clientType = clientType;
    }

    public String getUserTimeStampInMilli() {
        return userTimeStampInMilli;
    }

    public void setUserTimeStampInMilli(String userTimeStampInMilli) {
        this.userTimeStampInMilli = userTimeStampInMilli;
    }

    public void setProfileInfo(String profileInfo) {
        this.profileInfo = profileInfo;
    }

    public String getProfileInfo() {
        return profileInfo;
    }
}
