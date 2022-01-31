package se.tink.agent.sdk.models.payments;

public enum PaymentError implements ConnectivityError {
    EXECUTION_DATE_ON_A_NON_BUSINESS_DAY(
            ErrorReason.INVALID_EXECUTION_DATE, "The payment date is not a business day.", false),
    EXECUTION_DATE_TOO_CLOSE_IN_TIME(
            ErrorReason.INVALID_EXECUTION_DATE, "The payment date is too close in time.", false),
    EXECUTION_DATE_TOO_FAR_IN_FUTURE(
            ErrorReason.INVALID_EXECUTION_DATE,
            "The payment date is too far in the future.",
            false),
    REMITTANCE_INFORMATION_TOO_LONG_MESSAGE(
            ErrorReason.INVALID_REMITTANCE_INFORMATION_VALUE,
            "The payment reference exceeds the bank's maximum reference length.",
            false),
    REMITTANCE_INFORMATION_INVALID_TYPE(
            ErrorReason.INVALID_REMITTANCE_INFORMATION_TYPE,
            "The payment reference type is incorrect.",
            false),
    DEBTOR_ACCOUNT_MESSAGE_TOO_LONG(
            ErrorReason.INVALID_SOURCE_ACCOUNT_MESSAGE,
            "The source message exceeds the bank's maximum source message length.",
            false),
    INSUFFICIENT_FUNDS(
            ErrorReason.INVALID_AMOUNT,
            "The payment could not be initiated due to insufficient funds.",
            false),
    AMOUNT_LESS_THAN_BANK_LIMIT(
            ErrorReason.INVALID_AMOUNT,
            "The amount is less than the bank's minimum allowed amount.",
            false),
    AMOUNT_LARGER_THAN_BANK_LIMIT(
            ErrorReason.INVALID_AMOUNT,
            "The amount exceeds the bank's maximum allowed amount.",
            false),
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
    PERMISSIONS_NO_PAYMENT_PERMISSION(
            ErrorReason.INVALID_PERMISSIONS,
            "Insufficient permissions to initiate payments.",
            false),
    REJECTED(ErrorReason.PAYMENT_REJECTED, "The payment was rejected by the bank.", false),
    REJECTED_FRAUDULENT(
            ErrorReason.PAYMENT_REJECTED,
            "The payment was rejected by the bank due to being considered fraudulent.",
            false),
    REJECTED_REGULATORY(
            ErrorReason.PAYMENT_REJECTED,
            "The payment was rejected by the bank due to regulatory reasons.",
            false),
    REJECTED_DUPLICATE(
            ErrorReason.PAYMENT_REJECTED,
            "The payment could not be initiated due to an existing identical payment.",
            false),
    REJECTED_EXISTING_UNSIGNED_PAYMENTS(
            ErrorReason.PAYMENT_REJECTED,
            "The payment was rejected due to existing unsigned payments.",
            false),
    DEBTOR_ACCOUNT_CLOSED(ErrorReason.INVALID_DEBTOR, "The debtor account is closed.", false),
    DEBTOR_ACCOUNT_BLOCKED(ErrorReason.INVALID_DEBTOR, "The debtor account is blocked.", false),
    DEBTOR_ACCOUNT_NOT_PAYMENT_ACCOUNT(
            ErrorReason.INVALID_DEBTOR, "The debtor account cannot initiate payments.", false),
    CREDITOR_INVALID(ErrorReason.INVALID_CREDITOR, "The creditor is invalid.", false),
    CREDITOR_UNREGISTERED(
            ErrorReason.INVALID_CREDITOR,
            "The creditor is not registered as an approved payment beneficiary.",
            false),
    CREDITOR_NAME_REQUIRED(
            ErrorReason.INVALID_CREDITOR,
            "The creditor name is missing and required by the bank.",
            false),
    CREDITOR_ADDRESS_REQUIRED(
            ErrorReason.INVALID_CREDITOR,
            "The creditor address is missing and required by the bank.",
            false);

    private final ErrorReason reason;
    private final String displayMessage;
    private final boolean retryable;

    PaymentError(ErrorReason reason, String displayMessage, boolean retryable) {
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
