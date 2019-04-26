package se.tink.backend.aggregation.agents.exceptions.payment;

public class PaymentValidationException extends PaymentException {
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

    public String getPath() {
        return path;
    }

    @Override
    public String toString() {
        return path != null
                ? "Path that failed to validate : " + path + "\n" + super.toString()
                : super.toString();
    }
}
