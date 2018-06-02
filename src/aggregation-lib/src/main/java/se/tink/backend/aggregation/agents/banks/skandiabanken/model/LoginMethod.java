package se.tink.backend.aggregation.agents.banks.skandiabanken.model;

public class LoginMethod {
    private static final String APP_VERSION = "&appversion=Skandia%202.9.2%20%28587%29";

    private Integer typeOfLogin;
    private String loginUrl;
    private Integer expirationInSeconds;

    public Integer getTypeOfLogin() {
        return typeOfLogin;
    }

    public void setTypeOfLogin(Integer typeOfLogin) {
        this.typeOfLogin = typeOfLogin;
    }

    public String getLoginUrl() {
        return loginUrl + APP_VERSION;
    }

    public void setLoginUrl(String loginUrl) {
        this.loginUrl = loginUrl;
    }

    public Integer getExpirationInSeconds() {
        return expirationInSeconds;
    }

    public void setExpirationInSeconds(Integer expirationInSeconds) {
        this.expirationInSeconds = expirationInSeconds;
    }
}
