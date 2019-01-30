package se.tink.backend.aggregation.agents.banks.danskebank.v2.rpc;

public class LoginRequest {
    private String key;
    private String loginCode;
    private String loginId;

    public String getLoginCode() {
        return loginCode;
    }

    public String getLoginId() {
        return loginId;
    }

    public void setLoginCode(String loginCode) {
        this.loginCode = loginCode;
    }

    public void setLoginId(String loginId) {
        this.loginId = loginId;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
