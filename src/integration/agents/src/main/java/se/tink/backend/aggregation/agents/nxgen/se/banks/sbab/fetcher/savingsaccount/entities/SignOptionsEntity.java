package se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.fetcher.savingsaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SignOptionsEntity {
    @JsonProperty("bank_id_sign_redirect_url")
    private String bankIdSignRedirectUrl;

    @JsonProperty("autostart_token")
    private String autostartToken;

    public String getBankIdSignRedirectUrl() {
        return bankIdSignRedirectUrl;
    }

    public void setBankIdSignRedirectUrl(String bankIdSignRedirectUrl) {
        this.bankIdSignRedirectUrl = bankIdSignRedirectUrl;
    }

    public String getAutostartToken() {
        return autostartToken;
    }

    public void setAutostartToken(String autostartToken) {
        this.autostartToken = autostartToken;
    }
}
