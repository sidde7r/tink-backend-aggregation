package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.SdcConstants;

public class InitSessionRequest {
    private String appVersion = SdcConstants.Session.APP_VERSION;
    private String language = SdcConstants.Session.LANGUAGE;
    private String platform = SdcConstants.Session.PLATFORM;
    private String platformVersion = SdcConstants.Session.PLATFORM_VERSION;
    private String resolution = SdcConstants.Session.RESOLUTION;
    private String scale = SdcConstants.Session.SCALE;

    public String getAppVersion() {
        return appVersion;
    }

    public String getLanguage() {
        return language;
    }

    public String getPlatform() {
        return platform;
    }

    public String getPlatformVersion() {
        return platformVersion;
    }

    public String getResolution() {
        return resolution;
    }

    public String getScale() {
        return scale;
    }
}
