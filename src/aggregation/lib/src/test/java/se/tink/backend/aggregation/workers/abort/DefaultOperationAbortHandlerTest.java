package se.tink.backend.aggregation.workers.abort;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.workers.operation.OperationStatus;
import se.tink.backend.aggregation.workers.operation.OperationStatusManager;

@RunWith(MockitoJUnitRunner.class)
public class DefaultOperationAbortHandlerTest {

    @Mock private OperationStatusManager statusManager;
    @InjectMocks private DefaultOperationAbortHandler operationAbortHandler;

    @Test
    public void testHandleWhenOperationStatusIsEmptyThenOperationNotFountResultIsReturned() {
        // given
        String operationId = "a3ce3521-25ad-41c6-b361-25d141a585f5";
        when(statusManager.get(eq(operationId))).thenReturn(Optional.empty());

        // when
        Optional<OperationStatus> optionalStatus = operationAbortHandler.handle(operationId);

        // then
        assertFalse(optionalStatus.isPresent());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void
            testHandleWhenOperationStatusHasJustChangedToImpossibleToAbortThenImpossibleToAbortResultIsReturned() {
        // given
        String operationId = "a3ce3521-25ad-41c6-b361-25d141a585f5";
        when(statusManager.get(eq(operationId)))
                .thenReturn(
                        Optional.of(OperationStatus.STARTED),
                        Optional.of(OperationStatus.COMPLETED_WITHOUT_ABORT));
        when(statusManager.compareAndSet(
                        eq(operationId),
                        eq(OperationStatus.STARTED),
                        eq(OperationStatus.TRYING_TO_ABORT)))
                .thenReturn(false);

        // when
        Optional<OperationStatus> optionalStatus = operationAbortHandler.handle(operationId);

        // then
        assertTrue(optionalStatus.isPresent());
        assertEquals(OperationStatus.COMPLETED_WITHOUT_ABORT, optionalStatus.get());
    }

    @Test
    public void testHandleWhenOperationStatusIsTooLateToAbortThenTooLateToAbortResultIsReturned() {
        // given
        String operationId = "a3ce3521-25ad-41c6-b361-25d141a585f5";
        when(statusManager.get(eq(operationId)))
                .thenReturn(Optional.of(OperationStatus.COMPLETED_WITHOUT_ABORT));

        // when
        Optional<OperationStatus> optionalStatus = operationAbortHandler.handle(operationId);

        // then
        assertTrue(optionalStatus.isPresent());
        assertEquals(OperationStatus.COMPLETED_WITHOUT_ABORT, optionalStatus.get());
    }

    @Test
    public void testHandleWhenOperationStatusIsStartedThenAcceptedResultIsReturned() {
        // given
        String operationId = "a3ce3521-25ad-41c6-b361-25d141a585f5";
        when(statusManager.get(eq(operationId))).thenReturn(Optional.of(OperationStatus.STARTED));
        when(statusManager.compareAndSet(
                        eq(operationId),
                        eq(OperationStatus.STARTED),
                        eq(OperationStatus.TRYING_TO_ABORT)))
                .thenReturn(true);

        // when
        Optional<OperationStatus> optionalStatus = operationAbortHandler.handle(operationId);

        // then
        assertTrue(optionalStatus.isPresent());
        assertEquals(OperationStatus.TRYING_TO_ABORT, optionalStatus.get());
    }
}
