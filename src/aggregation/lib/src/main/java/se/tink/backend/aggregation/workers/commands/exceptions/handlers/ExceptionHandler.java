package se.tink.backend.aggregation.workers.commands.exceptions.handlers;

import com.google.common.base.Strings;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;

public interface ExceptionHandler<T extends Exception> {

    Class<T> getSupportedExceptionClass();

    AgentWorkerCommandResult handleException(T exception, ExceptionHandlerInput input);

    default String getStatusMessage(String message, String defaultMessage) {
        return Strings.isNullOrEmpty(message) ? defaultMessage : message;
    }
}
