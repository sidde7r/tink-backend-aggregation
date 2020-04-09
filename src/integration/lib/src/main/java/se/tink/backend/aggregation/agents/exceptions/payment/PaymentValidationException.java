package se.tink.backend.aggregation.agents.exceptions.payment;

public class PaymentValidationException extends PaymentException {
    private static final String INVALID_MINIMUM_AMOUNT =
            "The transfer amount, less than 1 SEK is not supported";

    private String path;

    public PaymentValidationException(String message, String path) {
        super(message);
        this.path = path;
    }

    public PaymentValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    public PaymentValidationException(String message, String path, Throwable cause) {
        super(message, cause);
        this.path = path;
    }

    public PaymentValidationException(String message) {
        super(message);
    }

    public String getPath() {
        return path;
    }

    @Override
    public String toString() {
        return path != null
                ? "Path that failed to validate : " + path + "\n" + super.toString()
                : super.toString();
    }

    public static PaymentValidationException invalidMinimumAmount() {
        return new PaymentValidationException(INVALID_MINIMUM_AMOUNT);
    }
}
