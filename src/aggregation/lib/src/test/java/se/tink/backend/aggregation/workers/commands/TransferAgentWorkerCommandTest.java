package se.tink.backend.aggregation.workers.commands;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepConstants.STEP_FINALIZE;

import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.PaymentControllerable;
import se.tink.backend.aggregation.agents.TransferExecutorNxgen;
import se.tink.backend.aggregation.agents.TypedPaymentControllerable;
import se.tink.backend.aggregation.agents.agent.Agent;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.backend.aggregation.rpc.RecurringPaymentRequest;
import se.tink.backend.aggregation.rpc.TransferRequest;
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

public class TransferAgentWorkerCommandTest {

    private static final AccountIdentifier SOURCE_ACCOUNT =
            AccountIdentifier.create(AccountIdentifierType.SE_BG, "7355837");

    private AgentWorkerCommandContext context = mock(AgentWorkerCommandContext.class);
    private AgentWorkerCommandMetricState metrics = mock(AgentWorkerCommandMetricState.class);

    @Before
    public void setup() {
        AgentWorkerCommandMetricState commandMetricState =
                mock(AgentWorkerCommandMetricState.class);
        when(metrics.init(any())).thenReturn(commandMetricState);
        MetricAction metricAction = mock(MetricAction.class);
        when(commandMetricState.buildAction(any())).thenReturn(metricAction);
    }

    @Test
    public void
            updates_transfer_signable_operation_object_with_debtor_when_typed_payment_controllerable()
                    throws PaymentException {
        // given
        TransferRequest transferRequest = mockTransferRequest();

        // and
        Agent agent = mockAgent(TypedPaymentControllerable.class, TransferExecutorNxgen.class);
        when(context.getAgent()).thenReturn(agent);
        mockAgentResponse(SOURCE_ACCOUNT, (TypedPaymentControllerable) agent);

        // and
        TransferAgentWorkerCommand transferAgentWorkerCommand =
                new TransferAgentWorkerCommand(context, transferRequest, metrics);

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
        when(context.getAgent()).thenReturn(agent);
        mockAgentResponse(SOURCE_ACCOUNT, (PaymentControllerable) agent);

        // and
        TransferAgentWorkerCommand transferAgentWorkerCommand =
                new TransferAgentWorkerCommand(context, recurringPaymentRequest, metrics);

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
        when(context.getAgent()).thenReturn(agent);

        // and
        TransferAgentWorkerCommand transferAgentWorkerCommand =
                new TransferAgentWorkerCommand(context, transferRequest, metrics);

        // when
        transferAgentWorkerCommand.doExecute();

        // then
        assertResultSourceNull();
    }

    private Agent mockAgent(Class<?>... extraInterfaces) {
        return mock(Agent.class, withSettings().extraInterfaces(extraInterfaces));
    }

    private void assertResultSourceMatch(AccountIdentifier sourceAccount) {
        ArgumentCaptor<SignableOperation> argCaptor = forClass(SignableOperation.class);
        verify(context).updateSignableOperation(argCaptor.capture());

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
        when(agent.getPaymentController(any())).thenReturn(Optional.of(paymentController));
    }

    private void mockAgentResponse(AccountIdentifier sourceAccount, PaymentControllerable agent)
            throws PaymentException {
        PaymentController paymentController = mockPaymentController(sourceAccount);
        when(agent.getPaymentController()).thenReturn(Optional.of(paymentController));
    }

    private PaymentController mockPaymentController(AccountIdentifier sourceAccount)
            throws PaymentException {
        PaymentController paymentController = mock(PaymentController.class);
        PaymentResponse paymentResponse = mock(PaymentResponse.class, RETURNS_DEEP_STUBS);
        Payment payment = mock(Payment.class, RETURNS_DEEP_STUBS);
        when(payment.getDebtor().getAccountIdentifier()).thenReturn(sourceAccount);

        PaymentMultiStepResponse multiStepResponse = mock(PaymentMultiStepResponse.class);
        when(multiStepResponse.getStep()).thenReturn(STEP_FINALIZE);
        when(multiStepResponse.getPayment()).thenReturn(payment);

        when(paymentController.sign(any())).thenReturn(multiStepResponse);
        when(paymentController.create(any())).thenReturn(paymentResponse);
        return paymentController;
    }

    private TransferRequest mockTransferRequest() {
        TransferRequest transferRequest = mock(TransferRequest.class, RETURNS_DEEP_STUBS);
        Credentials credentials = mockCredentials();
        when(transferRequest.getCredentials()).thenReturn(credentials);

        SignableOperation sourceSignableOperation = new SignableOperation(new Transfer());
        when(transferRequest.getSignableOperation()).thenReturn(sourceSignableOperation);
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
        when(recurringPaymentRequest.getCredentials()).thenReturn(credentials);

        SignableOperation sourceSignableOperation = new SignableOperation(new RecurringPayment());
        when(recurringPaymentRequest.getSignableOperation()).thenReturn(sourceSignableOperation);
        return recurringPaymentRequest;
    }
}
