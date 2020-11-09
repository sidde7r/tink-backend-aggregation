package se.tink.backend.aggregation.agents.exceptions.entity;

public class ErrorEntity {
    private String errorType;
    private String errorMessage;

    private ErrorEntity(String errorType, String errorMessage) {
        this.errorType = errorType;
        this.errorMessage = errorMessage;
    }

    public static ErrorEntity create(String errorType, String errorMessage) {
        return new ErrorEntity(errorType, errorMessage);
    }

    public String getErrorType() {
        return errorType;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
