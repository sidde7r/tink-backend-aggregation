package se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class KeyValueEntity {
    private String key;
    private String value;

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }
}
