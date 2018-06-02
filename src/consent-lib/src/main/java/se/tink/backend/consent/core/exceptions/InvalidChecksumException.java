package se.tink.backend.consent.core.exceptions;

public class InvalidChecksumException extends Exception {
    public InvalidChecksumException(String left, String right) {
        super(String.format("Checksums do not match (Left = '%s', Right = '%s')", left, right));
    }
}
