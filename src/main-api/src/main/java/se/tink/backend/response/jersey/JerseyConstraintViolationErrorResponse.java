package se.tink.backend.response.jersey;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class JerseyConstraintViolationErrorResponse {
    private String fieldName;
    private String errorMessage;

    JerseyConstraintViolationErrorResponse(String errorMessage, String fieldName) {
        this.errorMessage = errorMessage;
        this.fieldName = fieldName;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getFieldName() {
        return fieldName;
    }
}
