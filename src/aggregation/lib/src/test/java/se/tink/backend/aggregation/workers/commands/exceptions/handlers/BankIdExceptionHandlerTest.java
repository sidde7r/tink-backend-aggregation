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
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;
import se.tink.libraries.i18n_aggregation.LocalizableKey;
import se.tink.libraries.signableoperation.enums.SignableOperationStatuses;
import se.tink.libraries.signableoperation.rpc.SignableOperation;
import se.tink.libraries.transfer.rpc.Transfer;

@RunWith(JUnitParamsRunner.class)
public class BankIdExceptionHandlerTest extends ExceptionHandlerBaseTest {

    @Before
    public void setUp() {
        handler = new BankIdExceptionHandler();
        super.setUp();
        given(catalog.getString((LocalizableKey) Mockito.any())).willReturn("Catalog string");
    }

    @Test
    @Parameters(method = "bankIdErrors")
    public void shouldCancelMetricAndUpdateOperation(BankIdError bankIdError) {
        // given
        BankIdException exception = new BankIdException(bankIdError);
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
        then(metricAction).should().cancelled();
        then(context).should().updateSignableOperation(signableOperation);
    }

    @Test
    public void shouldFailMetricAndUpdateOperation() {
        // given
        BankIdException exception = new BankIdException(BankIdError.UNKNOWN);
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
        then(metricAction).should().failed();
        then(context).should().updateSignableOperation(signableOperation);
    }

    private Object[] bankIdErrors() {
        return new Object[] {
            BankIdError.TIMEOUT,
            BankIdError.CANCELLED,
            BankIdError.ALREADY_IN_PROGRESS,
            BankIdError.NO_CLIENT,
            BankIdError.AUTHORIZATION_REQUIRED
        };
    }
}
