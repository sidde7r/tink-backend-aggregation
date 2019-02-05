package se.tink.backend.aggregation.agents.nxgen.fr.banks.creditmutuel.fetchers.creditcard.pfm.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ValueEntity {
    private String position;
    private String type;
    private String state;
    private String value;

    public String getPosition() {
        return position;
    }

    public String getType() {
        return type;
    }

    public String getValue() {
        return value;
    }
}
