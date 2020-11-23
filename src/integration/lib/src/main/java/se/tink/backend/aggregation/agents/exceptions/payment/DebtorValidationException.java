package se.tink.backend.aggregation.agents.exceptions.payment;

import se.tink.libraries.signableoperation.enums.InternalStatus;

public class DebtorValidationException extends PaymentValidationException {
    public static final String DEFAULT_MESSAGE =
            "Could not validate the account, you are trying to pay from";
    private static final String IBAN_NOT_VALID =
            "Debtor account number is not in valid IBAN format";

    public DebtorValidationException(String message, String path, Throwable cause) {
        super(message, path, cause);
    }

    public DebtorValidationException(String message) {
        super(message);
    }

    public DebtorValidationException(String message, InternalStatus internalStatus) {
        super(message, internalStatus);
    }

    public static DebtorValidationException invalidIbanFormat(String path, Throwable cause) {
        return new DebtorValidationException(IBAN_NOT_VALID, path, cause);
    }
}
