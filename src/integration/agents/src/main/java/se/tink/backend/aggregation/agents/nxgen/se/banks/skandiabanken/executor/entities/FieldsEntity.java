package se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.executor.entities;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
public class FieldsEntity {

    private String code;
    private String field;
    private String message;

    public boolean isSignReference() {
        return "SignReference".equalsIgnoreCase(field);
    }
}
