package se.tink.backend.aggregation.agents.banks.skandiabanken.model;

public class LoginResponse {
    private int id; // A customer id.
    private boolean changeSecurityCode;
    private int timeOut;
    private int loginMethod;
    private String securityToken;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isChangeSecurityCode() {
        return changeSecurityCode;
    }

    public void setChangeSecurityCode(boolean changeSecurityCode) {
        this.changeSecurityCode = changeSecurityCode;
    }

    public int getTimeOut() {
        return timeOut;
    }

    public void setTimeOut(int timeOut) {
        this.timeOut = timeOut;
    }

    public int getLoginMethod() {
        return loginMethod;
    }

    public void setLoginMethod(int loginMethod) {
        this.loginMethod = loginMethod;
    }

    public String getSecurityToken() {
        return securityToken;
    }

    public void setSecurityToken(String securityToken) {
        this.securityToken = securityToken;
    }
}
