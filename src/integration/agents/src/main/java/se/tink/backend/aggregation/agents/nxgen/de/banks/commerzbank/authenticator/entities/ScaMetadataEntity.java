package se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ScaMetadataEntity {
    private String processContextId;

    private ScaMetadataEntity(String processContextId) {
        this.processContextId = processContextId;
    }

    public static ScaMetadataEntity create(String processContextId) {
        return new ScaMetadataEntity(processContextId);
    }
}
