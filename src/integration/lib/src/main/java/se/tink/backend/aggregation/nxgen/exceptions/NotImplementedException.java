package se.tink.backend.aggregation.nxgen.exceptions;

public class NotImplementedException extends RuntimeException {
    public NotImplementedException(String message) {
        super(message);
    }

    public static void throwIf(boolean condition) {
        throwIf(condition, "Not implemented");
    }

    public static void throwIf(boolean condition, String message) {
        if (condition) {
            throw new NotImplementedException(message);
        }
    }
}
