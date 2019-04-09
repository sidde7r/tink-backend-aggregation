package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v45.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LogonRequest {
    private String timeZoneOffsetInMilli;
    private String hardwareId;
    private String password;
    private String user;
    private String deviceModel;
    private String locale;
    private String rememberMe;
    private String userTimeStampInMilli;

    public void setTimeZoneOffsetInMilli(String timeZoneOffsetInMilli) {
        this.timeZoneOffsetInMilli = timeZoneOffsetInMilli;
    }

    public void setHardwareId(String hardwareId) {
        this.hardwareId = hardwareId;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setDeviceModel(String deviceModel) {
        this.deviceModel = deviceModel;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public void setRememberMe(String rememberMe) {
        this.rememberMe = rememberMe;
    }

    public void setUserTimeStampInMilli(String userTimeStampInMilli) {
        this.userTimeStampInMilli = userTimeStampInMilli;
    }

    public String getTimeZoneOffsetInMilli() {
        return timeZoneOffsetInMilli;
    }

    public String getHardwareId() {
        return hardwareId;
    }

    public String getPassword() {
        return password;
    }

    public String getUser() {
        return user;
    }

    public String getDeviceModel() {
        return deviceModel;
    }

    public String getLocale() {
        return locale;
    }

    public String getRememberMe() {
        return rememberMe;
    }

    public String getUserTimeStampInMilli() {
        return userTimeStampInMilli;
    }
}
