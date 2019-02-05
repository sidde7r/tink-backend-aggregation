package se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AppConfigEntity {
    private String version;
    private String currentDate;
    private String storeAppExterneConfigURL;
    private String cyberEditorialAnoURL;
    private String ssoStrategy;
    private String authBaseUrl;
    private String webSSOv3LogoutURL;
    private String webSSOv3CookieNamePrefix;
    private String webSSOv3LoginURL;
    private String webSSOv3LoginScreenURL;
    private String webSSOv3WebAPIBaseURL;
    private String webSSOv3WebAPIStepURL;
    private WebAPI2Entity webAPI2;
    private KeepAliveEntity keepAlive;

    public String getVersion() {
        return version;
    }

    public String getCurrentDate() {
        return currentDate;
    }

    public String getStoreAppExterneConfigURL() {
        return storeAppExterneConfigURL;
    }

    public String getCyberEditorialAnoURL() {
        return cyberEditorialAnoURL;
    }

    public String getSsoStrategy() {
        return ssoStrategy;
    }

    public String getAuthBaseUrl() {
        return authBaseUrl;
    }

    public String getWebSSOv3LogoutURL() {
        return webSSOv3LogoutURL;
    }

    public String getWebSSOv3CookieNamePrefix() {
        return webSSOv3CookieNamePrefix;
    }

    public String getWebSSOv3LoginURL() {
        return webSSOv3LoginURL;
    }

    public String getWebSSOv3LoginScreenURL() {
        return webSSOv3LoginScreenURL;
    }

    public String getWebSSOv3WebAPIBaseURL() {
        return webSSOv3WebAPIBaseURL;
    }

    public String getWebSSOv3WebAPIStepURL() {
        return webSSOv3WebAPIStepURL;
    }

    public WebAPI2Entity getWebAPI2() {
        return webAPI2;
    }

    public KeepAliveEntity getKeepAlive() {
        return keepAlive;
    }
}
