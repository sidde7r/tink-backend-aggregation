package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.authenticator.entities.oidcrequestobject;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class OpenbankingIntentId {

    private String value;
    private Boolean essential;

    public OpenbankingIntentId(String value, Boolean essential) {
        this.value = value;
        this.essential = essential;
    }
}
