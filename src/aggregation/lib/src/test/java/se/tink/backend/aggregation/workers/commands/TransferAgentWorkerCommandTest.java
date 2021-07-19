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
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepConstants.STEP_FINALIZE;

import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.PaymentControllerable;
import se.tink.backend.aggregation.agents.TransferExecutorNxgen;
import se.tink.backend.aggregation.agents.TypedPaymentControllerable;
import se.tink.backend.aggregation.agents.agent.Agent;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.backend.aggregation.rpc.RecurringPaymentRequest;
import se.tink.backend.aggregation.rpc.TransferRequest;
import se.tink.backend.aggregation.workers.commands.exceptions.ExceptionProcessor;
import se.tink.backend.aggregation.workers.context.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.metrics.AgentWorkerCommandMetricState;
import se.tink.backend.aggregation.workers.metrics.MetricAction;
import se.tink.backend.aggregationcontroller.v1.rpc.enums.CredentialsStatus;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.signableoperation.rpc.SignableOperation;
import se.tink.libraries.transfer.rpc.RecurringPayment;
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

    @Before
    public void setup() {
        given(metrics.init(any())).willReturn(commandMetricState);
        given(commandMetricState.buildAction(any())).willReturn(metricAction);
    }

    @Test
    public void
            updates_transfer_signable_operation_object_with_debtor_when_typed_payment_controllerable()
                    throws PaymentException {
        // given
        TransferRequest transferRequest = mockTransferRequest();

        // and
        Agent agent = mockAgent(TypedPaymentControllerable.class, TransferExecutorNxgen.class);
        given(context.getAgent()).willReturn(agent);
        mockAgentResponse(SOURCE_ACCOUNT, (TypedPaymentControllerable) agent);

        // and
        TransferAgentWorkerCommand transferAgentWorkerCommand =
                new TransferAgentWorkerCommand(
                        context, transferRequest, metrics, exceptionProcessor);

        // when
        transferAgentWorkerCommand.doExecute();

        // then
        assertResultSourceMatch(SOURCE_ACCOUNT);
    }

    @Test
    public void updates_recurring_signable_peration_object_with_debtor_when_payment_controllerable()
            throws PaymentException {
        // given
        RecurringPaymentRequest recurringPaymentRequest = mockRecurringPaymentRequest();

        // and
        Agent agent = mockAgent(PaymentControllerable.class, TransferExecutorNxgen.class);
        given(context.getAgent()).willReturn(agent);
        mockAgentResponse(SOURCE_ACCOUNT, (PaymentControllerable) agent);

        // and
        TransferAgentWorkerCommand transferAgentWorkerCommand =
                new TransferAgentWorkerCommand(
                        context, recurringPaymentRequest, metrics, exceptionProcessor);

        // when
        transferAgentWorkerCommand.doExecute();

        // then
        assertResultSourceMatch(SOURCE_ACCOUNT);
    }

    @Test
    public void
            does_not_update_transfer_signable_operation_object_with_debtor_when_not_controllerable() {
        // given
        TransferRequest transferRequest = mockTransferRequest();

        // and
        Agent agent = mockAgent(TransferExecutorNxgen.class);
        given(context.getAgent()).willReturn(agent);

        // and
        TransferAgentWorkerCommand transferAgentWorkerCommand =
                new TransferAgentWorkerCommand(
                        context, transferRequest, metrics, exceptionProcessor);

        // when
        transferAgentWorkerCommand.doExecute();

        // then
        assertResultSourceNull();
    }

    @Test
    public void shouldInvokeProcessExceptionWhenExceptionOccurs() {
        // given
        TransferRequest transferRequest = mockTransferRequest();
        doThrow(new BankIdException(BankIdError.AUTHORIZATION_REQUIRED))
                .when(transferRequest)
                .getProvider();

        // and
        Agent agent = mockAgent(TransferExecutorNxgen.class);
        given(context.getAgent()).willReturn(agent);

        // and
        TransferAgentWorkerCommand transferAgentWorkerCommand =
                new TransferAgentWorkerCommand(
                        context, transferRequest, metrics, exceptionProcessor);

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

    private void mockAgentResponse(
            AccountIdentifier sourceAccount, TypedPaymentControllerable agent)
            throws PaymentException {
        PaymentController paymentController = mockPaymentController(sourceAccount);
        given(agent.getPaymentController(any())).willReturn(Optional.of(paymentController));
    }

    private void mockAgentResponse(AccountIdentifier sourceAccount, PaymentControllerable agent)
            throws PaymentException {
        PaymentController paymentController = mockPaymentController(sourceAccount);
        given(agent.getPaymentController()).willReturn(Optional.of(paymentController));
    }

    private PaymentController mockPaymentController(AccountIdentifier sourceAccount)
            throws PaymentException {
        PaymentController paymentController = mock(PaymentController.class);
        PaymentResponse paymentResponse = mock(PaymentResponse.class, RETURNS_DEEP_STUBS);
        Payment payment = mock(Payment.class, RETURNS_DEEP_STUBS);
        when(payment.getDebtor().getAccountIdentifier()).thenReturn(sourceAccount);

        PaymentMultiStepResponse multiStepResponse = mock(PaymentMultiStepResponse.class);
        given(multiStepResponse.getStep()).willReturn((STEP_FINALIZE));
        given(multiStepResponse.getPayment()).willReturn((payment));

        given(paymentController.sign(any())).willReturn((multiStepResponse));
        given(paymentController.create(any())).willReturn(paymentResponse);
        return paymentController;
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

    private RecurringPaymentRequest mockRecurringPaymentRequest() {
        RecurringPaymentRequest recurringPaymentRequest =
                mock(RecurringPaymentRequest.class, RETURNS_DEEP_STUBS);
        Credentials credentials = mockCredentials();
        given(recurringPaymentRequest.getCredentials()).willReturn(credentials);

        SignableOperation sourceSignableOperation = new SignableOperation(new RecurringPayment());
        given(recurringPaymentRequest.getSignableOperation()).willReturn(sourceSignableOperation);
        return recurringPaymentRequest;
    }
}
