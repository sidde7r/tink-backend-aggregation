package se.tink.backend.aggregation.agents.banks.sbab.exception;

public class UnsupportedSignTypeException extends RuntimeException {
    public UnsupportedSignTypeException(SignType type) {
        super("Unsupported sign type: " + type);
    }
}
