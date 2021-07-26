package se.tink.backend.aggregation.workers.commands;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.withSettings;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.TransferExecutorNxgen;
import se.tink.backend.aggregation.agents.TypedPaymentControllerable;
import se.tink.backend.aggregation.agents.agent.Agent;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.backend.aggregation.rpc.TransferRequest;
import se.tink.backend.aggregation.workers.commands.exceptions.ExceptionProcessor;
import se.tink.backend.aggregation.workers.commands.exceptions.TransferAgentWorkerCommandExecutionException;
import se.tink.backend.aggregation.workers.commands.payment.PaymentExecutionService;
import se.tink.backend.aggregation.workers.commands.payment.executor.ExecutorResult;
import se.tink.backend.aggregation.workers.context.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.metrics.AgentWorkerCommandMetricState;
import se.tink.backend.aggregation.workers.metrics.MetricAction;
import se.tink.backend.aggregationcontroller.v1.rpc.enums.CredentialsStatus;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.signableoperation.rpc.SignableOperation;
import se.tink.libraries.transfer.rpc.Transfer;

@RunWith(MockitoJUnitRunner.class)
public class TransferAgentWorkerCommandTest {

    private static final AccountIdentifier SOURCE_ACCOUNT =
            AccountIdentifier.create(AccountIdentifierType.SE_BG, "7355837");

    @Mock private AgentWorkerCommandContext context;
    @Mock private AgentWorkerCommandMetricState metrics;
    @Mock private ExceptionProcessor exceptionProcessor;
    @Mock private AgentWorkerCommandMetricState commandMetricState;
    @Mock private MetricAction metricAction;
    @Mock private PaymentExecutionService paymentExecutionService;

    @Before
    public void setup() {
        given(metrics.init(any())).willReturn(commandMetricState);
        given(commandMetricState.buildAction(any())).willReturn(metricAction);
    }

    @Test
    public void shouldUpdateTransferSignableOperationWithDebtor()
            throws TransferAgentWorkerCommandExecutionException {
        // given
        TransferRequest transferRequest = mockTransferRequest();

        // and
        Agent agent = mockAgent(TypedPaymentControllerable.class, TransferExecutorNxgen.class);
        given(context.getAgent()).willReturn(agent);

        // and
        Payment payment = mock(Payment.class, RETURNS_DEEP_STUBS);
        given(payment.getDebtor().getAccountIdentifier()).willReturn(SOURCE_ACCOUNT);

        // and
        ExecutorResult executorResult = ExecutorResult.builder().payment(payment).build();
        given(paymentExecutionService.executePayment(any(), any(), any()))
                .willReturn(executorResult);

        // and
        TransferAgentWorkerCommand transferAgentWorkerCommand =
                new TransferAgentWorkerCommand(
                        context,
                        transferRequest,
                        metrics,
                        exceptionProcessor,
                        paymentExecutionService);

        // when
        transferAgentWorkerCommand.doExecute();

        // then
        assertResultSourceMatch(SOURCE_ACCOUNT);
    }

    @Test
    public void shouldNotUpdateTransferSignableOperationWithDebtor()
            throws TransferAgentWorkerCommandExecutionException {
        // given
        TransferRequest transferRequest = mockTransferRequest();

        // and
        Agent agent = mockAgent(TransferExecutorNxgen.class);
        given(context.getAgent()).willReturn(agent);

        // and
        ExecutorResult executorResult = ExecutorResult.builder().build();
        given(paymentExecutionService.executePayment(any(), any(), any()))
                .willReturn(executorResult);

        // and
        TransferAgentWorkerCommand transferAgentWorkerCommand =
                new TransferAgentWorkerCommand(
                        context,
                        transferRequest,
                        metrics,
                        exceptionProcessor,
                        paymentExecutionService);

        // when
        transferAgentWorkerCommand.doExecute();

        // then
        assertResultSourceNull();
    }

    @Test
    public void shouldInvokeExceptionProcessorWhenExceptionOccurs()
            throws TransferAgentWorkerCommandExecutionException {
        // given
        TransferRequest transferRequest = mockTransferRequest();

        // and
        Agent agent = mockAgent(TransferExecutorNxgen.class);
        given(context.getAgent()).willReturn(agent);

        // and
        doThrow(
                        new TransferAgentWorkerCommandExecutionException(
                                new BankIdException(BankIdError.AUTHORIZATION_REQUIRED)))
                .when(paymentExecutionService)
                .executePayment(any(), any(), any());

        // and
        TransferAgentWorkerCommand transferAgentWorkerCommand =
                new TransferAgentWorkerCommand(
                        context,
                        transferRequest,
                        metrics,
                        exceptionProcessor,
                        paymentExecutionService);

        // when
        transferAgentWorkerCommand.doExecute();

        // then
        then(exceptionProcessor).should().processException(any(), any());
    }

    private Agent mockAgent(Class<?>... extraInterfaces) {
        return mock(Agent.class, withSettings().extraInterfaces(extraInterfaces));
    }

    private void assertResultSourceMatch(AccountIdentifier sourceAccount) {
        ArgumentCaptor<SignableOperation> argCaptor = forClass(SignableOperation.class);
        then(context).should().updateSignableOperation(argCaptor.capture());

        SignableOperation signableOperation = argCaptor.getValue();
        Transfer transfer = signableOperation.getSignableObject(Transfer.class);
        assertThat(transfer.getSource()).isEqualTo(sourceAccount);
    }

    private void assertResultSourceNull() {
        ArgumentCaptor<SignableOperation> argCaptor = forClass(SignableOperation.class);
        verify(context).updateSignableOperation(argCaptor.capture());

        // and
        SignableOperation signableOperation = argCaptor.getValue();
        Transfer transfer = signableOperation.getSignableObject(Transfer.class);
        assertThat(transfer.getSource()).isNull();
    }

    private TransferRequest mockTransferRequest() {
        TransferRequest transferRequest = mock(TransferRequest.class, RETURNS_DEEP_STUBS);
        Credentials credentials = mockCredentials();
        given(transferRequest.getCredentials()).willReturn(credentials);

        SignableOperation sourceSignableOperation = new SignableOperation(new Transfer());
        given(transferRequest.getSignableOperation()).willReturn(sourceSignableOperation);
        return transferRequest;
    }

    private Credentials mockCredentials() {
        Credentials credentials = new Credentials();
        credentials.setStatus(CredentialsStatus.CREATED);
        return credentials;
    }
}
