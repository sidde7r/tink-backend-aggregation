package se.tink.backend.aggregation.agents.exceptions.payment;

public class CreditorValidationException extends PaymentValidationException {
    public static final String DEFAULT_MESSAGE = "Could not validate the destination account";
    private static final String IBAN_NOT_VALID =
            "Creditor account number is not in valid IBAN format";

    public CreditorValidationException(String message, String path, Throwable cause) {
        super(message, path, cause);
    }

    public static CreditorValidationException invalidIbanFormat(String path, Throwable cause) {
        return new CreditorValidationException(IBAN_NOT_VALID, path, cause);
    }
}
