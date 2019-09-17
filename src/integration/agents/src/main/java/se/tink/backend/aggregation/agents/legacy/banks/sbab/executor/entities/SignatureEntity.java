package se.tink.backend.aggregation.agents.banks.sbab.executor.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SignatureEntity {
    private String id;
    private String status;
    private String reference;

    public String getId() {
        return id;
    }

    public String getStatus() {
        return status;
    }

    public String getReference() {
        return reference;
    }
}
