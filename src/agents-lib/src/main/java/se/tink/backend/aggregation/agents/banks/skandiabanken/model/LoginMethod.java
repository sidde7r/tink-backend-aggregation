package se.tink.backend.aggregation.agents.banks.skandiabanken.model;

public class LoginMethod {
    private static final String APP_VERSION =
            "&appversion%3DSkandia%203.1.3%20%28654%29%26deviceversion%3DiPhone9%2C3%3B%20iOS%2012.1";

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
