package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.exception;

public class RequiredDataMissingException extends RuntimeException {

    public RequiredDataMissingException(String message) {
        super(message);
    }

    public RequiredDataMissingException(String message, Throwable cause) {
        super(message, cause);
    }
}
