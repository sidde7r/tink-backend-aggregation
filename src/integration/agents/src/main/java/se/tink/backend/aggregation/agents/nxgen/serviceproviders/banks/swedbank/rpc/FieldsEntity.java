package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FieldsEntity {
    private String field;
    private String message;

    public String getField() {
        return field;
    }

    public String getMessage() {
        return message;
    }
}
