package se.tink.backend.aggregation.workers.commands.exceptions;

public class TrasferAgentWorkerCommandExecutionException extends Exception {

    public TrasferAgentWorkerCommandExecutionException(Exception exception) {
        super(exception);
    }
}
