package se.tink.backend.aggregation.workers.commands.payment.executor;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;

import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.rpc.TransferRequest;
import se.tink.backend.aggregation.workers.commands.exceptions.TransferAgentWorkerCommandExecutionException;
import se.tink.libraries.transfer.rpc.Transfer;

@RunWith(MockitoJUnitRunner.class)
public class TransferExecutorTest {

    @Mock private Credentials credentials;

    @Mock private Transfer transfer;

    @Mock private TransferRequest transferRequest;

    @Mock private se.tink.backend.aggregation.agents.TransferExecutor transferExecutorAgent;

    private TransferExecutor transferExecutor;

    @Before
    public void setUp() {
        transferExecutor = new TransferExecutor(null);
        given(transferRequest.getTransfer()).willReturn(transfer);
    }

    @Test
    public void shouldHandlePayment() {
        // when
        boolean result = transferExecutor.canHandlePayment(transferExecutorAgent, transferRequest);

        // then
        Assert.assertTrue(result);
    }

    @Test
    public void shouldExecutePayment() throws Exception {
        // when
        ExecutorResult executorResult =
                transferExecutor.executePayment(
                        transferExecutorAgent, transferRequest, credentials);

        // then
        then(transferExecutorAgent).should().execute(transfer);
        Assertions.assertThat(executorResult.getOperationStatusMessage()).isNull();
        Assertions.assertThat(executorResult.getPayment()).isNull();
    }

    @Test
    public void shouldThrowTransferAgentWorkerCommandExecutionException() throws Exception {
        // given
        willThrow(new IllegalAccessException()).given(transferExecutorAgent).execute(transfer);

        // when
        Throwable throwable =
                Assertions.catchThrowable(
                        () ->
                                transferExecutor.executePayment(
                                        transferExecutorAgent, transferRequest, credentials));

        // then
        Assertions.assertThat(throwable)
                .isInstanceOf(TransferAgentWorkerCommandExecutionException.class);
    }
}
