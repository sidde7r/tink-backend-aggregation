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
import se.tink.backend.aggregation.workers.operation.RequestStatus;
import se.tink.backend.aggregation.workers.operation.RequestStatusManager;

@RunWith(MockitoJUnitRunner.class)
public class DefaultRequestAbortHandlerTest {

    @Mock private RequestStatusManager statusManager;
    @InjectMocks private DefaultRequestAbortHandler requestAbortHandler;

    @Test
    public void testHandleWhenOperationStatusIsEmptyThenOperationNotFountResultIsReturned() {
        // given
        String credentialsId = "a3ce3521-25ad-41c6-b361-25d141a585f5";
        when(statusManager.getByCredentialsId(eq(credentialsId))).thenReturn(Optional.empty());

        // when
        Optional<RequestStatus> optionalStatus = requestAbortHandler.handle(credentialsId);

        // then
        assertFalse(optionalStatus.isPresent());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void
            testHandleWhenOperationStatusHasJustChangedToImpossibleToAbortThenImpossibleToAbortResultIsReturned() {
        // given
        String credentialsId = "a3ce3521-25ad-41c6-b361-25d141a585f5";
        when(statusManager.getByCredentialsId(eq(credentialsId)))
                .thenReturn(
                        Optional.of(RequestStatus.STARTED),
                        Optional.of(RequestStatus.ABORTING_OPERATION_FAILED));
        when(statusManager.compareAndSetByCredentialsId(
                        eq(credentialsId),
                        eq(RequestStatus.STARTED),
                        eq(RequestStatus.TRYING_TO_ABORT)))
                .thenReturn(false);

        // when
        Optional<RequestStatus> optionalStatus = requestAbortHandler.handle(credentialsId);

        // then
        assertTrue(optionalStatus.isPresent());
        assertEquals(RequestStatus.ABORTING_OPERATION_FAILED, optionalStatus.get());
    }

    @Test
    public void testHandleWhenOperationStatusIsTooLateToAbortThenTooLateToAbortResultIsReturned() {
        // given
        String credentialsId = "a3ce3521-25ad-41c6-b361-25d141a585f5";
        when(statusManager.getByCredentialsId(eq(credentialsId)))
                .thenReturn(Optional.of(RequestStatus.ABORTING_OPERATION_FAILED));

        // when
        Optional<RequestStatus> optionalStatus = requestAbortHandler.handle(credentialsId);

        // then
        assertTrue(optionalStatus.isPresent());
        assertEquals(RequestStatus.ABORTING_OPERATION_FAILED, optionalStatus.get());
    }

    @Test
    public void testHandleWhenOperationStatusIsStartedThenAcceptedResultIsReturned() {
        // given
        String credentialsId = "a3ce3521-25ad-41c6-b361-25d141a585f5";
        when(statusManager.getByCredentialsId(eq(credentialsId)))
                .thenReturn(Optional.of(RequestStatus.STARTED));
        when(statusManager.compareAndSetByCredentialsId(
                        eq(credentialsId),
                        eq(RequestStatus.STARTED),
                        eq(RequestStatus.TRYING_TO_ABORT)))
                .thenReturn(true);

        // when
        Optional<RequestStatus> optionalStatus = requestAbortHandler.handle(credentialsId);

        // then
        assertTrue(optionalStatus.isPresent());
        assertEquals(RequestStatus.TRYING_TO_ABORT, optionalStatus.get());
    }
}
