package se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.authenticator;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ICSOAuthGrantTypes {
    CLIENT_CREDENTIALS("client_credentials"),

    AUTHORIZATION_CODE("authorization_code"),

    REFRESH_TOKEN("refresh_token");

    private final String value;

    @Override
    public String toString() {
        return value;
    }
}
