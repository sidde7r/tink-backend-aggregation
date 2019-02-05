package se.tink.backend.aggregation.agents.nxgen.fi.banks.op.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class OpBankAuthenticateCodeRequest {
    private String userkey;
    private String lang;

    public String getUserkey() {
        return userkey;
    }

    public OpBankAuthenticateCodeRequest setUserkey(String userkey) {
        this.userkey = userkey;
        return this;
    }

    public String getLang() {
        return lang;
    }

    public OpBankAuthenticateCodeRequest setLang(String lang) {
        this.lang = lang;
        return this;
    }
}
