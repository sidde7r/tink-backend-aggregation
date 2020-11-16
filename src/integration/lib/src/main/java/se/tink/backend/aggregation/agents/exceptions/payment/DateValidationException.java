package se.tink.backend.aggregation.agents.exceptions.payment;

import se.tink.libraries.signableoperation.enums.InternalStatus;

public class DateValidationException extends PaymentValidationException {
    public static final String DEFAULT_MESSAGE =
            "Could not validate the date you entered for the payment";
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

    public DateValidationException(
            String message,
            String path,
            InternalStatus internalStatus,
            IllegalArgumentException cause) {
        super(message, path, internalStatus, cause);
    }

    public static DateValidationException paymentDateTooCloseException() {
        return new DateValidationException(DATE_TOO_CLOSE_ERROR_MESSAGE);
    }

    public static DateValidationException paymentDateNotBusinessDayException() {
        return new DateValidationException(NOT_BUSINESS_DAY_ERROR_MESSAGE);
    }
}
