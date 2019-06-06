package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.executor.payment.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SignOptionsResponse {

    @JsonProperty("bank_id_sign_redirect_url")
    private String bankIdSignRedirectUrl;

    @JsonProperty("autostart_token")
    private String autostartToken;

    @JsonIgnore
    public String getAutostartToken() {
        return autostartToken;
    }
}
