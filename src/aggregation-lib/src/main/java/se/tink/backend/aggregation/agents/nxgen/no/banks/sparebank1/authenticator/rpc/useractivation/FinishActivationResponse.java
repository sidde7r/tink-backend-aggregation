package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.rpc.useractivation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
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

    public void setRememberMeToken(String rememberMeToken) {
        this.rememberMeToken = rememberMeToken;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getDeleteKey() {
        return deleteKey;
    }

    public void setDeleteKey(String deleteKey) {
        this.deleteKey = deleteKey;
    }

    public String getReactivateKey() {
        return reactivateKey;
    }

    public void setReactivateKey(String reactivateKey) {
        this.reactivateKey = reactivateKey;
    }

    public String getObfuscatedSsn() {
        return obfuscatedSsn;
    }

    public void setObfuscatedSsn(String obfuscatedSsn) {
        this.obfuscatedSsn = obfuscatedSsn;
    }

    public String getBankKey() {
        return bankKey;
    }

    public void setBankKey(String bankKey) {
        this.bankKey = bankKey;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }
}


