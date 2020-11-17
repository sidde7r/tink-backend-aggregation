package se.tink.backend.aggregation.agents.nxgen.serviceproviders.brokers.nordnet.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
public class BasicLoginRequest {
    private final String username;
    private final String password;

    @JsonProperty("session_lang")
    private final String sessionLanguage;

    public BasicLoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
        this.sessionLanguage = "en";
    }
}
