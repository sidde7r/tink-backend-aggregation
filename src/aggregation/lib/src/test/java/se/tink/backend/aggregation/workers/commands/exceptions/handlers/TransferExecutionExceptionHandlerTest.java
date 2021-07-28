package se.tink.backend.aggregation.workers.commands.exceptions.handlers;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;
import se.tink.libraries.signableoperation.enums.SignableOperationStatuses;
import se.tink.libraries.signableoperation.rpc.SignableOperation;
import se.tink.libraries.transfer.rpc.Transfer;

public class TransferExecutionExceptionHandlerTest extends ExceptionHandlerBaseTest {

    @Before
    public void setUp() {
        handler = new TransferExecutionExceptionHandler();
        super.setUp();
        given(catalog.getString((String) Mockito.any())).willReturn("Catalog string");
    }

    @Test
    public void shouldMarkMetricCancelled() {
        // given
        TransferExecutionException exception =
                TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                        .setInternalStatus("Internal Status")
                        .build();

        SignableOperation signableOperation = new SignableOperation();
        ExceptionHandlerInput input =
                ExceptionHandlerInput.builder()
                        .metricAction(metricAction)
                        .signableOperation(signableOperation)
                        .transfer(new Transfer())
                        .catalog(catalog)
                        .context(context)
                        .build();

        // when
        AgentWorkerCommandResult agentWorkerCommandResult =
                handler.handleException(exception, input);

        // then
        Assertions.assertThat(agentWorkerCommandResult).isEqualTo(AgentWorkerCommandResult.ABORT);
        then(metricAction).should().cancelled();
        then(context)
                .should()
                .updateSignableOperationStatus(
                        signableOperation,
                        exception.getSignableOperationStatus(),
                        exception.getUserMessage(),
                        exception.getInternalStatus());
    }

    @Test
    public void shouldMarkMetricFailed() {
        // given
        TransferExecutionException exception =
                TransferExecutionException.builder(SignableOperationStatuses.CREATED)
                        .setInternalStatus("Internal Status")
                        .build();

        SignableOperation signableOperation = new SignableOperation();
        ExceptionHandlerInput input =
                ExceptionHandlerInput.builder()
                        .metricAction(metricAction)
                        .signableOperation(signableOperation)
                        .transfer(new Transfer())
                        .catalog(catalog)
                        .context(context)
                        .build();

        // when
        AgentWorkerCommandResult agentWorkerCommandResult =
                handler.handleException(exception, input);

        // then
        Assertions.assertThat(agentWorkerCommandResult).isEqualTo(AgentWorkerCommandResult.ABORT);
        then(metricAction).should().failed();
        then(context)
                .should()
                .updateSignableOperationStatus(
                        signableOperation,
                        exception.getSignableOperationStatus(),
                        exception.getUserMessage(),
                        exception.getInternalStatus());
    }
}
