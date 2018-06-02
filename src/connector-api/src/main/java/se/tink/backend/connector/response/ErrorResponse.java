package se.tink.backend.connector.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@SuppressWarnings("WeakerAccess")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    private String errorMessage;
    private String externalAccountId;
    private String externalUserId;
    private String externalTransactionId;

    public ErrorResponse(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getExternalAccountId() {
        return externalAccountId;
    }

    public void setExternalAccountId(String externalAccountId) {
        this.externalAccountId = externalAccountId;
    }

    public String getExternalUserId() {
        return externalUserId;
    }

    public void setExternalUserId(String externalUserId) {
        this.externalUserId = externalUserId;
    }

    public void setExternalTransactionId(String externalTransactionId) {
        this.externalTransactionId = externalTransactionId;
    }

    public String getExternalTransactionId() {
        return externalTransactionId;
    }
}
