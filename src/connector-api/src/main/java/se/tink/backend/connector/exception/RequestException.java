package se.tink.backend.connector.exception;

import javax.ws.rs.core.Response;
import se.tink.backend.connector.exception.error.RequestError;

public class RequestException extends Exception {

    private final RequestError error;
    private String externalUserId;
    private String externalAccountId;
    private String externalTransactionId;
    private final String errorMessage;
    private final Response.Status status;
    private final String logMessage;

    public RequestException(RequestError error) {
        super(String.format("Cause: %s.%s", error.getClass().getSimpleName(), error.name()));
        this.errorMessage = error.getErrorMessage();
        this.logMessage = error.getLogMessage();
        this.status = error.getStatus();
        this.error = error;
    }

    public RequestError getError() {
        return error;
    }

    public RequestException withExternalUserId(String externalUserId) {
        this.externalUserId = externalUserId;
        return this;
    }

    public RequestException withExternalAccountId(String externalAccountId) {
        this.externalAccountId = externalAccountId;
        return this;
    }

    public RequestException withExternalTransactionId(String externalTransactionId) {
        this.externalTransactionId = externalTransactionId;
        return this;
    }

    public String getExternalUserId() {
        return externalUserId;
    }

    public String getExternalAccountId() {
        return externalAccountId;
    }

    public String getExternalTransactionId() {
        return externalTransactionId;
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
}
