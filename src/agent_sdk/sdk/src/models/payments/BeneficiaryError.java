package se.tink.agent.sdk.models.payments;

public enum BeneficiaryError implements ConnectivityError {
    AUTHORIZATION_CANCELLED(
            ErrorReason.AUTHORIZATION_CANCELLED,
            "Authorization of payment was cancelled. Please try again.",
            false),
    AUTHORIZATION_TIMEOUT(
            ErrorReason.AUTHORIZATION_TIMEOUT,
            "Authorization of payment timed out. Please try again.",
            false),
    AUTHORIZATION_FAILED(
            ErrorReason.AUTHORIZATION_FAILED, "Authorization of payment failed.", false),
    PERMISSIONS_BANKID_EXTENDED_USE_NOT_ENABLED(
            ErrorReason.INVALID_PERMISSIONS,
            "Activation of extended use for BankId required.",
            false),
    BENEFICIARY_INVALID(ErrorReason.INVALID_BENEFICIARY, "The beneficiary is invalid.", false),
    BENEFICIARY_INVALID_ACCOUNT_TYPE(
            ErrorReason.INVALID_BENEFICIARY,
            "The beneficiary account number type is not supported by the bank.",
            false),
    BENEFICIARY_INVALID_ACCOUNT_NUMBER(
            ErrorReason.INVALID_BENEFICIARY, "The beneficiary account number is invalid.", false),
    BENEFICIARY_NAME_REQUIRED(
            ErrorReason.INVALID_BENEFICIARY,
            "The beneficiary name is missing and required by the bank.",
            false);

    private final ErrorReason reason;
    private final String displayMessage;
    private final boolean retryable;

    BeneficiaryError(ErrorReason reason, String displayMessage, boolean retryable) {
        this.reason = reason;
        this.displayMessage = displayMessage;
        this.retryable = retryable;
    }

    @Override
    public String getReason() {
        return reason.name();
    }

    @Override
    public String getDisplayMessage() {
        return displayMessage;
    }

    @Override
    public boolean isRetryable() {
        return retryable;
    }
}
