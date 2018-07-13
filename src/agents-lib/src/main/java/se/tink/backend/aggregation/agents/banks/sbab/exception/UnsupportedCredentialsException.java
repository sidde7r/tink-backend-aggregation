package se.tink.backend.aggregation.agents.banks.sbab.exception;

import se.tink.backend.aggregation.rpc.CredentialsTypes;

public class UnsupportedCredentialsException extends RuntimeException {

    public UnsupportedCredentialsException(CredentialsTypes type) {
        super("Unsupported credentials type: " + type);
    }
}
