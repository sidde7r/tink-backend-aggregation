package se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.exception;

public class UnsuccessfulApiCallException extends AbstractAgentException {

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
