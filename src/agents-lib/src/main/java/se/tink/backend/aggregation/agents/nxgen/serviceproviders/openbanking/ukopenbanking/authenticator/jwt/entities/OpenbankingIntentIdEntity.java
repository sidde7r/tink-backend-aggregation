package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.authenticator.jwt.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class OpenbankingIntentIdEntity {
    private String value;
    private boolean essential;

    public OpenbankingIntentIdEntity(String value, boolean essential) {
        this.value = value;
        this.essential = essential;
    }
}
