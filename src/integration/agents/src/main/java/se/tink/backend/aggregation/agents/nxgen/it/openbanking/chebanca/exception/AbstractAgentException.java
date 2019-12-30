package se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.exception;

public abstract class AbstractAgentException extends RuntimeException {

    AbstractAgentException(String s) {
        super(s);
    }

    AbstractAgentException(String s, Throwable throwable) {
        super(s, throwable);
    }

    AbstractAgentException(Throwable throwable) {
        super(throwable);
    }
}
