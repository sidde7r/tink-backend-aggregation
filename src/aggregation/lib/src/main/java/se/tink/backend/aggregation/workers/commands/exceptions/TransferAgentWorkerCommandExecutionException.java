package se.tink.backend.aggregation.workers.commands.exceptions;

public class TransferAgentWorkerCommandExecutionException extends Exception {

    public TransferAgentWorkerCommandExecutionException(Exception exception) {
        super(exception);
    }
}
