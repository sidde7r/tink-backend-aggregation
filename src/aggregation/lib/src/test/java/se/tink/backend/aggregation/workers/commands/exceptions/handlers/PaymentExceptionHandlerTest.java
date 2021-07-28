package se.tink.backend.aggregation.workers.commands.exceptions.handlers;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;
import se.tink.libraries.signableoperation.enums.InternalStatus;
import se.tink.libraries.signableoperation.enums.SignableOperationStatuses;
import se.tink.libraries.signableoperation.rpc.SignableOperation;
import se.tink.libraries.transfer.rpc.Transfer;

public class PaymentExceptionHandlerTest extends ExceptionHandlerBaseTest {

    @Before
    public void setUp() {
        handler = new PaymentExceptionHandler();
        super.setUp();
        given(catalog.getString((String) Mockito.any())).willReturn("Catalog string");
    }

    @Test
    public void shouldMarkMetricFailedAndFailOperation() {
        // given
        PaymentException exception =
                new PaymentException(InternalStatus.INVALID_DESTINATION_MESSAGE);
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
        Assertions.assertThat(signableOperation.getStatus())
                .isEqualTo(SignableOperationStatuses.FAILED);
        Assertions.assertThat(signableOperation.getStatusMessage()).isEqualTo("Catalog string");
        Assertions.assertThat(signableOperation.getInternalStatus())
                .isEqualTo(InternalStatus.INVALID_DESTINATION_MESSAGE.toString());
        then(metricAction).should().failed();
        then(context).should().updateSignableOperation(signableOperation);
    }
}
