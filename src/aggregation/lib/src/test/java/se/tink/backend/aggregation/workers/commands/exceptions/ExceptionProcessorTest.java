package se.tink.backend.aggregation.workers.commands.exceptions;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Set;
import org.junit.Test;
import org.mockito.internal.util.collections.Sets;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.backend.aggregation.workers.commands.exceptions.handlers.ExceptionHandler;
import se.tink.backend.aggregation.workers.commands.exceptions.handlers.ExceptionHandlerInput;

public class ExceptionProcessorTest {

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void shouldInvokeProperHandler() {
        // given
        ExceptionHandler<BankIdException> bankIdExceptionHandler = mock(ExceptionHandler.class);
        when(bankIdExceptionHandler.getSupportedExceptionClass()).thenReturn(BankIdException.class);

        Set<ExceptionHandler> exceptionHandlers = Sets.newSet(bankIdExceptionHandler);
        ExceptionProcessor exceptionProcessor = new ExceptionProcessor(exceptionHandlers);

        ExceptionHandlerInput input = ExceptionHandlerInput.builder().build();
        BankIdException bankIdException = new BankIdException(BankIdError.AUTHORIZATION_REQUIRED);

        // when
        exceptionProcessor.processException(bankIdException, input);

        // then
        verify(bankIdExceptionHandler).handleException(bankIdException, input);
    }
}
