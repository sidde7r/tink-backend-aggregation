package se.tink.backend.aggregation.workers.commands.exceptions;

public class EmptyDebugLogException extends Exception {
    @Override
    public String getMessage() {
        return "The content of the debug log was empty. This file will not be stored.";
    }
}
