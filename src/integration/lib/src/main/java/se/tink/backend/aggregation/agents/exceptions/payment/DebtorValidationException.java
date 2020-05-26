package se.tink.backend.aggregation.agents.exceptions.payment;

public class DebtorValidationException extends PaymentValidationException {
    private static final String IBAN_NOT_VALID =
            "Debtor account number is not in valid IBAN format";

    public DebtorValidationException(String message, String path, Throwable cause) {
        super(message, path, cause);
    }

    public DebtorValidationException(String message) {
        super(message);
    }

    public static DebtorValidationException invalidIbanFormat(String path, Throwable cause) {
        return new DebtorValidationException(IBAN_NOT_VALID, path, cause);
    }
}
