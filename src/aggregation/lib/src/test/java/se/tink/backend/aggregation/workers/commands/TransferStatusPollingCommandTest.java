package se.tink.backend.aggregation.workers.commands;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.util.Optional;
import lombok.SneakyThrows;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.agents.TypedPaymentControllerable;
import se.tink.backend.aggregation.agents.agent.Agent;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.backend.aggregation.rpc.TransferRequest;
import se.tink.backend.aggregation.workers.context.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.signableoperation.enums.SignableOperationStatuses;
import se.tink.libraries.signableoperation.rpc.SignableOperation;
import se.tink.libraries.transfer.mocks.TransferMock;
import se.tink.libraries.transfer.rpc.RemittanceInformation;
import se.tink.libraries.transfer.rpc.Transfer;

@RunWith(MockitoJUnitRunner.class)
public class TransferStatusPollingCommandTest {

    private static final String ACCOUNT_NUMBER = "56943546619";
    private static final double AMOUNT_IN_SEK = 1.0;

    private TransferStatusPollingCommand command;

    @Test
    public void doExecuteShouldSucceedIfSettlementCompleted() {
        // given
        final AgentWorkerCommandContext contextMock =
                createContextWithPaymentStatus(PaymentStatus.SETTLEMENT_COMPLETED);
        final SignableOperation signableOperation = createSignableOperation();
        final TransferRequest transferRequestMock = createTransferRequest(signableOperation);

        command = new TransferStatusPollingCommand(contextMock, transferRequestMock, 10L, 3);

        // when
        AgentWorkerCommandResult commandResult = command.doExecute();

        // then
        assertThat(commandResult).isEqualTo(AgentWorkerCommandResult.CONTINUE);
        verify(signableOperation).setStatus(SignableOperationStatuses.SETTLEMENT_COMPLETED);
        verify(contextMock).updateSignableOperation(signableOperation);
    }

    @Test
    public void doExecuteShouldFailIfSignableOperationIsFailed() {
        // given
        final AgentWorkerCommandContext contextMock =
                createContextWithPaymentStatus(PaymentStatus.SETTLEMENT_COMPLETED);
        final SignableOperation signableOperation = createFailedSignableOperation();
        final TransferRequest transferRequestMock = createTransferRequest(signableOperation);

        command = new TransferStatusPollingCommand(contextMock, transferRequestMock, 10L, 3);

        // when
        AgentWorkerCommandResult commandResult = command.doExecute();

        // then
        assertThat(commandResult).isEqualTo(AgentWorkerCommandResult.ABORT);
        verify(signableOperation, times(0)).setStatus(any());
        verify(contextMock, times(0)).updateSignableOperation(signableOperation);
    }

    @Test
    public void doExecuteShouldFailIfAgentNotSupportPolling() {
        // given
        final AgentWorkerCommandContext contextMock = createContextWithAgentNotSupportPolling();
        final SignableOperation signableOperation = createSignableOperation();
        final TransferRequest transferRequestMock = createTransferRequest(signableOperation);

        command = new TransferStatusPollingCommand(contextMock, transferRequestMock, 10L, 3);

        // when
        AgentWorkerCommandResult commandResult = command.doExecute();

        // then
        assertThat(commandResult).isEqualTo(AgentWorkerCommandResult.ABORT);
        verify(signableOperation, times(0)).setStatus(any());
        verify(contextMock, times(0)).updateSignableOperation(signableOperation);
    }

    @Test
    public void doExecuteShouldFailIfSettlementNotCompleted() {
        // given
        final AgentWorkerCommandContext contextMock =
                createContextWithPaymentStatus(PaymentStatus.SIGNED);
        final SignableOperation signableOperation = createSignableOperation();
        final TransferRequest transferRequestMock = createTransferRequest(signableOperation);

        command = new TransferStatusPollingCommand(contextMock, transferRequestMock, 10L, 3);

        // when
        AgentWorkerCommandResult commandResult = command.doExecute();

        // then
        assertThat(commandResult).isEqualTo(AgentWorkerCommandResult.ABORT);
        verify(signableOperation, times(0)).setStatus(any());
        verify(contextMock, times(0)).updateSignableOperation(signableOperation);
    }

    private static AgentWorkerCommandContext createContextWithPaymentStatus(
            PaymentStatus paymentStatus) {
        final PaymentController paymentController = createPaymentController(paymentStatus);
        final TypedPaymentControllerable agentMock = createAgent(paymentController);

        return createContext(agentMock);
    }

    private static SignableOperation createSignableOperation() {
        final SignableOperation signableOperation = mock(SignableOperation.class);
        when(signableOperation.getStatus()).thenReturn(SignableOperationStatuses.EXECUTED);

        return signableOperation;
    }

    private static SignableOperation createFailedSignableOperation() {
        final SignableOperation signableOperation = mock(SignableOperation.class);
        when(signableOperation.getStatus()).thenReturn(SignableOperationStatuses.FAILED);

        return signableOperation;
    }

    @SneakyThrows
    private static PaymentController createPaymentController(PaymentStatus paymentStatus) {
        final PaymentController paymentController = mock(PaymentController.class);
        final Payment payment = mock(Payment.class);
        when(payment.getStatus()).thenReturn(paymentStatus);
        when(paymentController.fetch(any())).thenReturn(new PaymentResponse(payment));

        return paymentController;
    }

    private static TypedPaymentControllerable createAgent(PaymentController paymentController) {
        final TypedPaymentControllerable agentMock =
                mock(TypedPaymentControllerable.class, withSettings().extraInterfaces(Agent.class));
        when(agentMock.getPaymentController(any())).thenReturn(Optional.of(paymentController));

        return agentMock;
    }

    private static AgentWorkerCommandContext createContextWithAgentNotSupportPolling() {
        final Agent agentMock = mock(Agent.class);
        final AgentWorkerCommandContext contextMock = mock(AgentWorkerCommandContext.class);
        when(contextMock.getAgent()).thenReturn(agentMock);

        return contextMock;
    }

    private static AgentWorkerCommandContext createContext(TypedPaymentControllerable agent) {
        final AgentWorkerCommandContext contextMock = mock(AgentWorkerCommandContext.class);
        when(contextMock.getAgent()).thenReturn((Agent) agent);

        return contextMock;
    }

    private static TransferRequest createTransferRequest(SignableOperation signableOperation) {
        final TransferRequest transferRequestMock = mock(TransferRequest.class);

        final Transfer transferMock = createTransfer();
        when(transferRequestMock.getSignableOperation()).thenReturn(signableOperation);
        when(transferRequestMock.getProvider()).thenReturn(mock(Provider.class));
        when(transferRequestMock.getTransfer()).thenReturn(transferMock);

        return transferRequestMock;
    }

    private static Transfer createTransfer() {
        return TransferMock.bankTransfer()
                .to(AccountIdentifier.create(AccountIdentifierType.SE, ACCOUNT_NUMBER))
                .withAmountInSEK(AMOUNT_IN_SEK)
                .withRemittanceInformation(new RemittanceInformation())
                .build();
    }
}
