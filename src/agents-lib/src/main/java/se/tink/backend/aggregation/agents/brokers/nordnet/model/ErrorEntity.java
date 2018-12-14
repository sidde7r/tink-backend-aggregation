package se.tink.backend.aggregation.agents.brokers.nordnet.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ErrorEntity {
    private String code;
    private String message;

    @JsonIgnore
    public boolean isBankIdAlreadyInProgressError() {
        return "ALREADY_IN_PROGRESS".equalsIgnoreCase(code);
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
