package se.tink.backend.aggregation.agents.nxgen.es.banks.ing.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public final class LoginDocument {

    @JsonProperty("document")
    private String username;

    @JsonProperty("documentType")
    private int usernameType;

    private LoginDocument(String username, int usernameType) {
        this.username = username;
        this.usernameType = usernameType;
    }

    public static LoginDocument create(String username, int usernameType) {
        return new LoginDocument(username, usernameType);
    }
}
