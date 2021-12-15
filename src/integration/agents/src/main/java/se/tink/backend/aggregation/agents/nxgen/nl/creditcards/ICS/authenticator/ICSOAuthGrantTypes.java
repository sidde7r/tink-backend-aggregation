package se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.authenticator;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ICSOAuthGrantTypes {
    @JsonProperty("client_credentials")
    CLIENT_CREDENTIALS("client_credentials"),

    @JsonProperty("authorization_code")
    AUTHORIZATION_CODE("authorization_code"),

    @JsonProperty("refresh_token")
    REFRESH_TOKEN("refresh_token");

    private final String value;

    @Override
    public String toString() {
        return value;
    }
}
