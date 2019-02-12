package se.tink.libraries.creditsafe.consumermonitoring.api;

public class CreditSafeResponse {
    private String errorCode;
    private String errorMessage;
    private String status;

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getStatus() {
        return status;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
