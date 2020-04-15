package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.client.exception;

public class UnsuccessfulApiCallException extends RuntimeException {

    public UnsuccessfulApiCallException(String s) {
        super(s);
    }

    public UnsuccessfulApiCallException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public UnsuccessfulApiCallException(Throwable throwable) {
        super(throwable);
    }
}
