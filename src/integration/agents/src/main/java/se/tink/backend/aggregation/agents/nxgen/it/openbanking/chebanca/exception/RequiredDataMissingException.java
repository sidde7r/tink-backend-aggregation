package se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.exception;

public class RequiredDataMissingException extends AbstractAgentException {

    public RequiredDataMissingException(String s) {
        super(s);
    }

    public RequiredDataMissingException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public RequiredDataMissingException(Throwable throwable) {
        super(throwable);
    }
}
