package se.tink.backend.aggregation.workers.commands.exceptions;

import com.google.inject.Inject;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.workers.commands.exceptions.handlers.DefaultExceptionHandler;
import se.tink.backend.aggregation.workers.commands.exceptions.handlers.ExceptionHandler;
import se.tink.backend.aggregation.workers.commands.exceptions.handlers.ExceptionHandlerInput;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;

@SuppressWarnings("rawtypes")
public class ExceptionProcessor {

    private final Map<Class, ExceptionHandler> exceptionHandlers;

    @Inject
    public ExceptionProcessor(Set<ExceptionHandler> exceptionHandlersSet) {
        this.exceptionHandlers =
                exceptionHandlersSet.stream()
                        .collect(
                                Collectors.toMap(
                                        ExceptionHandler::getSupportedExceptionClass,
                                        Function.identity()));
    }

    @SuppressWarnings("unchecked")
    public AgentWorkerCommandResult processException(Exception e, ExceptionHandlerInput input) {
        DefaultExceptionHandler defaultExceptionHandler = new DefaultExceptionHandler();
        return Optional.ofNullable(exceptionHandlers.get(e.getClass()))
                .orElse(defaultExceptionHandler)
                .handleException(e, input);
    }
}
