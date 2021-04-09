package se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class Result {
    private String code;
    private String message;

    public String getCode() {
        return this.code;
    }

    public String getMessage() {
        return this.message;
    }
}
