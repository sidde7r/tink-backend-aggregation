package se.tink.backend.aggregation.workers.commands.payment;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.TypedPaymentControllerable;
import se.tink.backend.aggregation.rpc.TransferRequest;
import se.tink.backend.aggregation.workers.commands.exceptions.TransferAgentWorkerCommandExecutionException;
import se.tink.backend.aggregation.workers.commands.payment.executor.Executor;
import se.tink.backend.aggregation.workers.commands.payment.executor.ExecutorResult;
import se.tink.backend.aggregation.workers.commands.payment.executor.PaymentExecutorFactory;
import se.tink.libraries.payment.rpc.Payment;

@RunWith(MockitoJUnitRunner.class)
public class PaymentExecutionServiceImplTest {

    @Mock private Credentials credentials;

    @Mock private TransferRequest transferRequest;

    @Mock private Executor executor;

    @Mock private PaymentExecutorFactory paymentExecutorFactory;

    private PaymentExecutionService paymentExecutionService;

    @Before
    public void setUp() {
        given(paymentExecutorFactory.createExecutorsChain()).willReturn(executor);
        paymentExecutionService = new PaymentExecutionServiceImpl(paymentExecutorFactory);
    }

    @Test
    public void shouldExecutePayment() throws TransferAgentWorkerCommandExecutionException {
        // given
        ExecutorResult executorResult =
                ExecutorResult.builder()
                        .payment(new Payment.Builder().build())
                        .operationStatusMessage("Message")
                        .build();

        TypedPaymentControllerable agent = mock(TypedPaymentControllerable.class);

        given(executor.executePayment(any(), any(), any())).willReturn(executorResult);

        // when
        ExecutorResult result =
                paymentExecutionService.executePayment(agent, credentials, transferRequest);

        // then
        then(executor).should().executePayment(agent, transferRequest, credentials);

        Assertions.assertThat(result.getOperationStatusMessage())
                .isEqualTo(executorResult.getOperationStatusMessage());
        Assertions.assertThat(result.getPayment()).isEqualTo(executorResult.getPayment());
    }
}
