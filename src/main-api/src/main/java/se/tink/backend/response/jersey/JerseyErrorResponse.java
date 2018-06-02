package se.tink.backend.response.jersey;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * General API response class which creates a common structure for Jersey API errors.
 * This makes it more consistent for API consumers.
 */
@JsonInclude(JsonInclude.Include.NON_NULL) // If a field is null, it won't show in the error response.
public class JerseyErrorResponse {
    private String errorMessage;
    private String errorCode;
    private String errorDetails;

    JerseyErrorResponse(String errorMessage, String errorCode) {
        this.errorMessage = errorMessage;
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getErrorDetails() {
        return errorDetails;
    }

    public void setErrorDetails(String errorDetails) {
        this.errorDetails = errorDetails;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
