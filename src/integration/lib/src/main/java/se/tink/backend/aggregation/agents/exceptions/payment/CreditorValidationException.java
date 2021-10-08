package se.tink.backend.aggregation.agents.exceptions.payment;

import se.tink.libraries.signableoperation.enums.InternalStatus;

public class CreditorValidationException extends PaymentValidationException {
    public static final String DEFAULT_MESSAGE = "Could not validate the creditor account.";
    private static final String IBAN_NOT_VALID =
            "Creditor account number is not in valid IBAN format.";
    private static final String MISSING_CREDITOR_NAME_OR_ADDRESS =
            "Missing creditor name or address.";

    public CreditorValidationException(String message, String path, Throwable cause) {
        super(message, path, cause);
    }

    public CreditorValidationException(String message, InternalStatus internalStatus) {
        super(message, internalStatus);
    }

    public static CreditorValidationException invalidAccount() {
        return new CreditorValidationException(
                DEFAULT_MESSAGE, InternalStatus.INVALID_DESTINATION_ACCOUNT);
    }

    public static CreditorValidationException invalidIbanFormat(String path, Throwable cause) {
        return new CreditorValidationException(IBAN_NOT_VALID, path, cause);
    }

    public static CreditorValidationException missingCreditorNameOrAddress() {
        return new CreditorValidationException(
                MISSING_CREDITOR_NAME_OR_ADDRESS, InternalStatus.INVALID_DESTINATION_ACCOUNT);
    }
}
