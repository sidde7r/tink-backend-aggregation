package se.tink.backend.aggregation.agents.banks.sbab.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InitBankIdResponse {

    @JsonProperty("pending_authorization_code")
    private String pendingAuthorizationCode;
    @JsonProperty("autostart_token")
    private String autostartToken;

    public String getPendingAuthorizationCode() {
        return pendingAuthorizationCode;
    }
}
