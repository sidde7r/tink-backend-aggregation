package se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.entities;

import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.SpankkiConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ClientInfoEntity {
    private String platformName = SpankkiConstants.Request.CLIENT_INFO_PLATFORM_NAME;
    private String appVersion = SpankkiConstants.Request.CLIENT_INFO_APP_VERSION;
    private String lang = SpankkiConstants.Request.CLIENT_INFO_LANG;
    private String platformType = SpankkiConstants.Request.CLIENT_INFO_PLATFORM_TYPE;
    private String appName = SpankkiConstants.Request.CLIENT_INFO_APP_NAME;
    private String platformVersion = SpankkiConstants.Request.CLIENT_INFO_PLATFORM_VERSION;
    private String deviceModel = SpankkiConstants.Request.CLIENT_INFO_DEVICE_MODEL;

    public String getPlatformName() {
        return platformName;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public String getLang() {
        return lang;
    }

    public String getPlatformType() {
        return platformType;
    }

    public String getAppName() {
        return appName;
    }

    public String getPlatformVersion() {
        return platformVersion;
    }

    public String getDeviceModel() {
        return deviceModel;
    }
}
