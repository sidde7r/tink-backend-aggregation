package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class JsonWebTokenEntity {

    private String rawToken;

    public String getRawToken() {
        return rawToken;
    }
}
