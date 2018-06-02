package se.tink.backend.exception.jersey;

import javax.ws.rs.core.Response;

/**
 * General class representing Jersey exceptions which can be thrown from the Main API.
 */
public class JerseyRequestException extends Exception {

    private final JerseyRequestError error;
    private final String errorCode;
    private String errorDetails;
    private final String errorMessage;
    private final Response.Status status;
    private final String logMessage;

    JerseyRequestException(JerseyRequestError error) {
        super(String.format("Cause: %s.%s", error.getClass().getSimpleName(), error.getErrorCode()));
        this.errorMessage = error.getErrorMessage();
        this.logMessage = error.getLogMessage();
        this.status = error.getStatus();
        this.errorCode = error.getErrorCode();
        this.error = error;
    }

    public JerseyRequestError getError() {
        return error;
    }

    public JerseyRequestException withErrorDetails(String errorDetails) {
        this.errorDetails = errorDetails;
        return this;
    }

    public String getErrorDetails() {
        return errorDetails;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public Response.Status getStatus() {
        return status;
    }

    public String getLogMessage() {
        return logMessage;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
