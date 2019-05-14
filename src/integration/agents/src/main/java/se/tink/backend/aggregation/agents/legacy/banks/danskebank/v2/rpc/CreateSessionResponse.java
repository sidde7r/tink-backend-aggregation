package se.tink.backend.aggregation.agents.banks.danskebank.v2.rpc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateSessionResponse extends AbstractResponse {
    @JsonProperty("LoginType")
    protected String loginType;

    @JsonProperty("Settings")
    protected SettingsEntity settings;

    @JsonProperty("LoginInfo")
    protected LoginInfoEntity loginInfo;

    public String getMagicKey() {
        return magicKey;
    }

    public void setMagicKey(String magicKey) {
        this.magicKey = magicKey;
    }

    public LoginInfoEntity getLoginInfo() {
        return loginInfo;
    }

    public void setLoginInfo(LoginInfoEntity loginInfo) {
        this.loginInfo = loginInfo;
    }

    public String getLoginType() {
        return loginType;
    }

    public void setLoginType(String loginType) {
        this.loginType = loginType;
    }

    public SettingsEntity getSettings() {
        return settings;
    }

    public void setSettings(SettingsEntity settings) {
        this.settings = settings;
    }
}
