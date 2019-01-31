package se.tink.backend.aggregation.agents.banks.sbab.exception;

import se.tink.backend.aggregation.agents.banks.sbab.model.response.SignType;

public class UnsupportedSignTypeException extends RuntimeException {
    public UnsupportedSignTypeException(SignType type) {
        super("Unsupported sign type: " + type);
    }
}
