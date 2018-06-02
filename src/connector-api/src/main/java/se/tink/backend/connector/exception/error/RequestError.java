package se.tink.backend.connector.exception.error;

import javax.ws.rs.core.Response;
import se.tink.backend.connector.exception.RequestException;

public enum RequestError {

    USER_NOT_FOUND("Could not find the user.", Response.Status.UNAUTHORIZED),
    USER_ALREADY_EXISTS("The user already exists.", Response.Status.CONFLICT),
    MARKET_NOT_FOUND("The specified market does not exist.", Response.Status.NOT_FOUND),

    CREDENTIALS_NOT_FOUND(GeneralMessage.SERVER_ERROR_MESSAGE, "Invalid state: no credentials exist for the user.",
            Response.Status.UNAUTHORIZED),
    CREDENTIALS_MORE_THAN_1(GeneralMessage.SERVER_ERROR_MESSAGE,
            "Invalid state: more than one credentials exist for the user.", Response.Status.UNAUTHORIZED),

    ACCOUNT_NOT_FOUND("Could not find the account.", Response.Status.BAD_REQUEST),
    NO_ACCOUNTS_FOUND("Could not find any accounts for the user.", Response.Status.PRECONDITION_FAILED),

    TRANSACTION_ALREADY_DELETED("Transaction has already been deleted.", Response.Status.GONE),
    TRANSACTION_ID_NOT_MATCHING("The specified ID in the request URI and in the payload does not match.",
            Response.Status.BAD_REQUEST),
    TRANSACTION_NOT_FOUND("Could not find the transaction.", Response.Status.NOT_FOUND),
    TRANSACTION_CONFLICT("The transaction already exists.", Response.Status.CONFLICT),

    INVALID_URL("The url is invalid.", Response.Status.BAD_REQUEST),
    NOT_HTTPS("The url must use https.", Response.Status.BAD_REQUEST),
    UNREGISTERED_WEBHOOK_DOMAIN("The url domain has not been registered.", Response.Status.BAD_REQUEST),
    WEBHOOK_OVERLAP("The webhook overlaps with what has already been registered.", Response.Status.CONFLICT),
    WEBHOOK_NOT_FOUND("Could not find the webhook.", Response.Status.NOT_FOUND),

    INVALID_DESCRIPTION("Description is not valid.", Response.Status.BAD_REQUEST),
    VALIDATION_FAILED("Validation of the sent data failed.", Response.Status.BAD_REQUEST);

    private final String errorMessage;
    private String logMessage;
    private Response.Status status;

    RequestError(String errorMessage, Response.Status status) {
        this.errorMessage = errorMessage;
        this.logMessage = errorMessage;
        this.status = status;
    }

    RequestError(String errorMessage, String logMessage, Response.Status status) {
        this.errorMessage = errorMessage;
        this.logMessage = logMessage;
        this.status = status;
    }

    private class GeneralMessage {
        static final String SERVER_ERROR_MESSAGE = "An internal server error occurred.";
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getLogMessage() {
        return logMessage;
    }

    public Response.Status getStatus() {
        return status;
    }

    public RequestException exception() {
        return new RequestException(this);
    }
}
