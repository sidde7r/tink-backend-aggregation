package se.tink.libraries.payloadparser;

class PayloadParserException extends RuntimeException {
    PayloadParserException(final String message) {
        super(message);
    }

    PayloadParserException(final Throwable cause) {
        super(cause);
    }
}
