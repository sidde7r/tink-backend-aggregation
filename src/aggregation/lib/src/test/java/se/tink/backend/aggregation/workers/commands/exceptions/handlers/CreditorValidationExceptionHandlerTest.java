package se.tink.backend.aggregation.workers.commands.exceptions.handlers;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.exceptions.payment.CreditorValidationException;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;
import se.tink.libraries.signableoperation.enums.InternalStatus;
import se.tink.libraries.signableoperation.enums.SignableOperationStatuses;
import se.tink.libraries.signableoperation.rpc.SignableOperation;
import se.tink.libraries.transfer.rpc.Transfer;

public class CreditorValidationExceptionHandlerTest extends ExceptionHandlerBaseTest {

    @Before
    public void setUp() {
        handler = new CreditorValidationExceptionHandler();
        super.setUp();
        given(catalog.getString((String) Mockito.any())).willReturn("Catalog string");
    }

    @Test
    public void shouldMarkMetricCancelledAndCancelOperation() {
        // given
        CreditorValidationException exception =
                new CreditorValidationException(
                        "Message", InternalStatus.INTERNAL_TRANSFER_NOT_SUPPORTED);
        SignableOperation signableOperation = new SignableOperation();
        ExceptionHandlerInput input =
                ExceptionHandlerInput.builder()
                        .metricAction(metricAction)
                        .transfer(new Transfer())
                        .signableOperation(signableOperation)
                        .catalog(catalog)
                        .context(context)
                        .build();

        // when
        AgentWorkerCommandResult agentWorkerCommandResult =
                handler.handleException(exception, input);

        // then
        Assertions.assertThat(agentWorkerCommandResult).isEqualTo(AgentWorkerCommandResult.ABORT);
        Assertions.assertThat(signableOperation.getStatus())
                .isEqualTo(SignableOperationStatuses.CANCELLED);
        Assertions.assertThat(signableOperation.getStatusMessage()).isEqualTo("Catalog string");
        Assertions.assertThat(signableOperation.getInternalStatus())
                .isEqualTo(InternalStatus.INTERNAL_TRANSFER_NOT_SUPPORTED.toString());
        then(metricAction).should().cancelled();
        then(context).should().updateSignableOperation(signableOperation);
    }
}
