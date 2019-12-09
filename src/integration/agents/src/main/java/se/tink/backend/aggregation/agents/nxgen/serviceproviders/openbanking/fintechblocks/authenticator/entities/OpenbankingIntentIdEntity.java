package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintechblocks.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class OpenbankingIntentIdEntity {

    private String value;

    public OpenbankingIntentIdEntity(String value) {
        this.value = value;
    }
}
