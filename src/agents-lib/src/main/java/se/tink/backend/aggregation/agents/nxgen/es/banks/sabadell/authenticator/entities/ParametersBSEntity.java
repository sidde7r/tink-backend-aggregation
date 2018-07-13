package se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ParametersBSEntity {
    private String key;
    private String value;

    public String getKey() {
        return key;
    }

    public ParametersBSEntity setKey(String key) {
        this.key = key;
        return this;
    }

    public String getValue() {
        return value;
    }

    public ParametersBSEntity setValue(String value) {
        this.value = value;
        return this;
    }
}
