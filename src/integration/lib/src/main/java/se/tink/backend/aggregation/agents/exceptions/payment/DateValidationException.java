package se.tink.backend.aggregation.agents.exceptions.payment;

public class DateValidationException extends PaymentValidationException {
    private static final String DATE_TOO_CLOSE_ERROR_MESSAGE =
            "The date when the money will reach the recipient is too close.";
    private static final String NOT_BUSINESS_DAY_ERROR_MESSAGE =
            "The payment date is not a business day";

    public DateValidationException(String message, String path, Throwable cause) {
        super(message, path, cause);
    }

    public DateValidationException(String message) {
        super(message);
    }

    public static DateValidationException paymentDateTooCloseException() {
        return new DateValidationException(DATE_TOO_CLOSE_ERROR_MESSAGE);
    }

    public static DateValidationException paymentDateNotBusinessDayException() {
        return new DateValidationException(NOT_BUSINESS_DAY_ERROR_MESSAGE);
    }
}
