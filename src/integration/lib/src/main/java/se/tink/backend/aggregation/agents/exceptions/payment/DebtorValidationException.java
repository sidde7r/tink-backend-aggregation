package se.tink.backend.aggregation.agents.exceptions.payment;

import se.tink.libraries.signableoperation.enums.InternalStatus;

public class DebtorValidationException extends PaymentValidationException {
    public static final String DEFAULT_MESSAGE =
            "Could not validate the account, you are trying to pay from.";
    private static final String IBAN_NOT_VALID =
            "Debtor account number is not in valid IBAN format.";
    private static final String SAME_USER_MESSAGE = "Debtor and creditor can not be the same user";

    public DebtorValidationException(String message, String path, Throwable cause) {
        super(message, path, cause);
    }

    public DebtorValidationException(String message) {
        super(message);
    }

    public DebtorValidationException(String message, InternalStatus internalStatus) {
        super(message, internalStatus);
    }

    public static DebtorValidationException invalidAccount() {
        return new DebtorValidationException(
                DEFAULT_MESSAGE, InternalStatus.INVALID_SOURCE_ACCOUNT);
    }

    public static DebtorValidationException invalidIbanFormat(String path, Throwable cause) {
        return new DebtorValidationException(IBAN_NOT_VALID, path, cause);
    }

    public static DebtorValidationException canNotFromSameUser() {
        return new DebtorValidationException(
                SAME_USER_MESSAGE, InternalStatus.INTERNAL_TRANSFER_NOT_SUPPORTED);
    }
}
