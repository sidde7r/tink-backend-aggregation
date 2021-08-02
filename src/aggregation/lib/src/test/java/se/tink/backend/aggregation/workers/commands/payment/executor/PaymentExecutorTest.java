package se.tink.backend.aggregation.workers.commands.payment.executor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.Optional;
import java.util.UUID;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.PaymentControllerable;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepConstants;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.backend.aggregation.nxgen.storage.Storage;
import se.tink.backend.aggregation.rpc.TransferRequest;
import se.tink.backend.aggregation.workers.commands.exceptions.TransferAgentWorkerCommandExecutionException;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.signableoperation.enums.InternalStatus;
import se.tink.libraries.transfer.rpc.Transfer;

@RunWith(MockitoJUnitRunner.class)
public class PaymentExecutorTest {

    @Mock private Credentials credentials;

    @Mock private PaymentControllerable paymentControllerable;

    @Mock private PaymentController paymentController;

    @Mock private PaymentResponse paymentResponse;

    @Mock private TransferRequest transferRequest;

    private PaymentExecutor paymentExecutor;

    @Before
    public void setUp() {
        paymentExecutor = new PaymentExecutor(null);
        given(transferRequest.getTransfer()).willReturn(createTransfer());
    }

    @Test
    public void shouldHandlePayment() {
        given(paymentControllerable.getPaymentController())
                .willReturn(Optional.of(paymentController));

        // when
        boolean result = paymentExecutor.canHandlePayment(paymentControllerable, transferRequest);

        // then
        Assert.assertTrue(result);
    }

    @Test
    public void shouldExecutePayment() throws Exception {
        // given
        given(paymentControllerable.getPaymentController())
                .willReturn(Optional.of(paymentController));
        given(paymentResponse.getStorage()).willReturn(mock(Storage.class));
        given(paymentController.create(any())).willReturn(paymentResponse);

        PaymentMultiStepResponse firstStepResponse = mockFirstStepResponse();

        Payment payment = new Payment.Builder().build();
        PaymentMultiStepResponse secondStepResponse = mockSecondStepResponse(payment);
        given(paymentController.sign(any())).willReturn(firstStepResponse, secondStepResponse);

        // when
        ExecutorResult executorResult =
                paymentExecutor.executePayment(paymentControllerable, transferRequest, credentials);

        // then
        Assertions.assertThat(executorResult.getPayment()).isEqualTo(payment);
    }

    @Test
    public void shouldThrowTransferAgentWorkerCommandExecutionException() throws PaymentException {
        // given

        given(paymentControllerable.getPaymentController())
                .willReturn(Optional.of(paymentController));
        willThrow(new PaymentException(InternalStatus.INVALID_DESTINATION_MESSAGE))
                .given(paymentController)
                .create(any());

        // when
        Throwable throwable =
                Assertions.catchThrowable(
                        () ->
                                paymentExecutor.executePayment(
                                        paymentControllerable, transferRequest, credentials));

        // then
        Assertions.assertThat(throwable)
                .isInstanceOf(TransferAgentWorkerCommandExecutionException.class);
    }

    private Transfer createTransfer() {
        AccountIdentifier accountIdentifier = new IbanIdentifier("AL35202111090000000001234567");
        Transfer transfer = new Transfer();
        transfer.setDestination(accountIdentifier);
        transfer.setAmount(BigDecimal.ONE, "PLN");
        transfer.setDueDate(Date.valueOf("2020-08-13"));
        transfer.setId(UUID.fromString("e4586bed-032a-5ae6-9883-331cd94c4ffa"));
        return transfer;
    }

    private PaymentMultiStepResponse mockFirstStepResponse() {
        PaymentMultiStepResponse response = mock(PaymentMultiStepResponse.class);
        given(response.getStorage()).willReturn(mock(Storage.class));
        given(response.getPayment()).willReturn(mock(Payment.class));
        given(response.getStep()).willReturn(AuthenticationStepConstants.STEP_INIT);
        return response;
    }

    private PaymentMultiStepResponse mockSecondStepResponse(Payment payment) {
        PaymentMultiStepResponse response = mock(PaymentMultiStepResponse.class);
        given(response.getPayment()).willReturn(payment);
        given(response.getStep()).willReturn(AuthenticationStepConstants.STEP_FINALIZE);
        return response;
    }
}
