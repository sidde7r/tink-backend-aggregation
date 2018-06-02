package se.tink.backend.connector.response;

public class ConstraintViolationErrorResponse extends ErrorResponse {
    private final String fieldName;

    public ConstraintViolationErrorResponse(String errorMessage, String fieldName) {
        super(errorMessage);
        this.fieldName = fieldName;
    }

    public String getFieldName() {
        return fieldName;
    }
}
