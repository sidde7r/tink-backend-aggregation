package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.jwt.entities;

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
