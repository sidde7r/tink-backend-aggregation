package se.tink.backend.aggregation.workers.commands.exceptions.handlers;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;
import se.tink.libraries.i18n_aggregation.LocalizableKey;
import se.tink.libraries.signableoperation.enums.InternalStatus;
import se.tink.libraries.signableoperation.enums.SignableOperationStatuses;
import se.tink.libraries.signableoperation.rpc.SignableOperation;
import se.tink.libraries.transfer.rpc.Transfer;

@RunWith(JUnitParamsRunner.class)
public class BankServiceExceptionHandlerTest extends ExceptionHandlerBaseTest {

    @Before
    public void setUp() {
        handler = new BankServiceExceptionHandler();
        super.setUp();
        given(catalog.getString((LocalizableKey) Mockito.any())).willReturn("Catalog string");
    }

    @Test
    @Parameters(method = "input")
    public void shouldMarkMetricUnavailableAndFailOperation(
            BankServiceError bankServiceError, InternalStatus internalStatus) {
        // given
        BankServiceException exception = new BankServiceException(bankServiceError);
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
                .isEqualTo(SignableOperationStatuses.FAILED);
        Assertions.assertThat(signableOperation.getStatusMessage()).isEqualTo("Catalog string");
        Assertions.assertThat(signableOperation.getInternalStatus())
                .isEqualTo(internalStatus.toString());
        then(metricAction).should().unavailable();
        then(context).should().updateSignableOperation(signableOperation);
    }

    private Object[] input() {
        return new Object[] {
            new Object[] {BankServiceError.ACCESS_EXCEEDED, InternalStatus.RATE_LIMIT_EXCEEDED},
            new Object[] {
                BankServiceError.BANK_SIDE_FAILURE, InternalStatus.BANK_SERVICE_UNAVAILABLE
            }
        };
    }
}
