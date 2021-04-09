package se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ProductEntity {
    private String internalKey;

    public String getInternalKey() {
        return internalKey;
    }
}
