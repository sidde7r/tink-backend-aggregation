package se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.authenticator.page;

public final class AttemptsLimitExceededException extends Exception {

    public AttemptsLimitExceededException(String message, Throwable cause) {
        super(message, cause);
    }
}
