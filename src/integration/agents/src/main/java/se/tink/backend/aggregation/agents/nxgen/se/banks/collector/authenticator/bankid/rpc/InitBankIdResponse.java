package se.tink.backend.aggregation.agents.nxgen.se.banks.collector.authenticator.bankid.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InitBankIdResponse {
    @JsonProperty("autostart_token")
    private String autostartToken;

    private String sessionid;

    public String getAutostartToken() {
        return autostartToken;
    }

    public String getSessionid() {
        return sessionid;
    }
}
