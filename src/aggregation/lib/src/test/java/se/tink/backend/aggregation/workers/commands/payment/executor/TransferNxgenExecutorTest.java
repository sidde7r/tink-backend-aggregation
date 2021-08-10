package se.tink.backend.aggregation.workers.commands.payment.executor;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;

import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.PaymentControllerable;
import se.tink.backend.aggregation.agents.TransferExecutorNxgen;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;
import se.tink.backend.aggregation.rpc.TransferRequest;
import se.tink.backend.aggregation.workers.commands.exceptions.TransferAgentWorkerCommandExecutionException;
import se.tink.libraries.transfer.rpc.Transfer;

@RunWith(MockitoJUnitRunner.class)
public class TransferNxgenExecutorTest {

    @Mock private Credentials credentials;

    @Mock private Transfer transfer;

    @Mock private PaymentControllerable agent;

    @Mock private AgentImplementingTwoInterfaces executorNgen;

    @Mock private TransferRequest transferRequest;

    private TransferNxgenExecutor transferExecutor;

    @Before
    public void setUp() {
        transferExecutor = new TransferNxgenExecutor(null);
        given(transferRequest.getTransfer()).willReturn(transfer);
    }

    @Test
    public void shouldHandlePayment() {
        // given
        given(agent.getPaymentController()).willReturn(Optional.empty());

        // when
        boolean result = transferExecutor.canHandlePayment(agent, transferRequest);

        // then
        Assert.assertTrue(result);
    }

    @Test
    public void shouldExecutePayment() throws Exception {
        // when
        ExecutorResult executorResult =
                transferExecutor.executePayment(executorNgen, transferRequest, credentials);
        // then
        then(executorNgen).should().execute(transfer);
        Assertions.assertThat(executorResult.getOperationStatusMessage()).isNull();
        Assertions.assertThat(executorResult.getPayment()).isNull();
    }

    @Test
    public void shouldThrowTransferAgentWorkerCommandExecutionException() {
        // given
        willThrow(new IllegalStateException()).given(executorNgen).execute(transfer);

        // when
        Throwable throwable =
                Assertions.catchThrowable(
                        () ->
                                transferExecutor.executePayment(
                                        executorNgen, transferRequest, credentials));

        // then
        Assertions.assertThat(throwable)
                .isInstanceOf(TransferAgentWorkerCommandExecutionException.class);
    }

    static class AgentImplementingTwoInterfaces
            implements PaymentControllerable, TransferExecutorNxgen {

        @Override
        public Optional<PaymentController> getPaymentController() {
            return Optional.empty();
        }

        @Override
        public Optional<String> execute(Transfer transfer) {
            return Optional.empty();
        }
    }
}
