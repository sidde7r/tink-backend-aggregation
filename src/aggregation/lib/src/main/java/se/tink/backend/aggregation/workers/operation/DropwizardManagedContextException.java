package se.tink.backend.aggregation.workers.operation;

public class DropwizardManagedContextException extends RuntimeException {

    public DropwizardManagedContextException(Throwable cause) {
        super("Dropwizard managed context unexpected exception", cause);
    }
}
