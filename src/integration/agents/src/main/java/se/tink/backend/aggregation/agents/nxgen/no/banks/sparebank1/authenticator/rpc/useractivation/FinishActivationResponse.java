package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.rpc.useractivation;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FinishActivationResponse {
    private String rememberMeToken;
    private String fullName;
    private String deleteKey;
    private String reactivateKey;
    private String obfuscatedSsn;
    private String bankKey;
    private String bankName;

    public String getRememberMeToken() {
        return rememberMeToken;
    }

    public String getFullName() {
        return fullName;
    }

    public String getDeleteKey() {
        return deleteKey;
    }

    public String getReactivateKey() {
        return reactivateKey;
    }

    public String getObfuscatedSsn() {
        return obfuscatedSsn;
    }

    public String getBankKey() {
        return bankKey;
    }

    public String getBankName() {
        return bankName;
    }
}
