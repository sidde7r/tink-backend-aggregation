package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.fetchers.einvoice.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ReferenceEntity {
    public enum Type {
        OCR
    }
    private String value;
    private Type type;

    public String getValue() {
        return value;
    }

    public Type getType() {
        return type;
    }
}
