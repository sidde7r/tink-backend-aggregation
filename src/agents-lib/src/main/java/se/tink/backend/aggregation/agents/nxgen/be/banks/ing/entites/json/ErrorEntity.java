package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.entites.json;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ErrorEntity {
    private String code;
    private String text;

    public String getCode() {
        return code;
    }

    public String getText() {
        return text;
    }
}
