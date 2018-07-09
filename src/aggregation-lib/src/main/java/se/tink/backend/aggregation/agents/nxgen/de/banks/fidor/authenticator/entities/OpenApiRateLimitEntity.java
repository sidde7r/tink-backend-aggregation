package se.tink.backend.aggregation.agents.nxgen.de.banks.fidor.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class OpenApiRateLimitEntity {

    private int limit;
    private int remaining;
    private int reset;

    public int getLimit() {
        return limit;
    }

    public int getRemaining() {
        return remaining;
    }

    public int getReset() {
        return reset;
    }
}
