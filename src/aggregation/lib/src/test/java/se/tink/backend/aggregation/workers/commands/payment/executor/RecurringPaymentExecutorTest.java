package se.tink.backend.aggregation.workers.commands.payment.executor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;

import java.math.BigDecimal;
import java.sql.Date;
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
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepConstants;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.backend.aggregation.nxgen.storage.Storage;
import se.tink.backend.aggregation.rpc.RecurringPaymentRequest;
import se.tink.backend.aggregation.workers.commands.exceptions.TransferAgentWorkerCommandExecutionException;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.signableoperation.enums.InternalStatus;
import se.tink.libraries.transfer.rpc.RecurringPayment;

@RunWith(MockitoJUnitRunner.class)
public class RecurringPaymentExecutorTest {

    @Mock private Credentials credentials;

    @Mock private PaymentControllerable agent;

    @Mock private PaymentController paymentController;

    @Mock private RecurringPaymentRequest recurringPaymentRequest;

    private RecurringPaymentExecutor recurringPaymentExecutor;

    @Before
    public void setUp() {
        recurringPaymentExecutor = new RecurringPaymentExecutor(null);
        given(agent.getPaymentController()).willReturn(Optional.of(paymentController));
    }

    @Test
    public void shouldHandlePayment() {
        // when
        boolean result = recurringPaymentExecutor.canHandlePayment(agent, recurringPaymentRequest);

        // then
        Assert.assertTrue(result);
    }

    @Test
    public void shouldExecutePayment() throws Exception {
        //    given
        RecurringPayment recurringPayment = createRecurringPayment();
        given(recurringPaymentRequest.getRecurringPayment()).willReturn(recurringPayment);
        PaymentResponse paymentResponse = mockPaymentResponse();
        given(paymentController.create(any())).willReturn(paymentResponse);

        PaymentMultiStepResponse firstStepResponse = mockFirstPaymentStepResponse();
        Payment payment = new Payment.Builder().build();
        PaymentMultiStepResponse secondStepResponse = mockSecondPaymentStepResponse(payment);
        given(paymentController.sign(any())).willReturn(firstStepResponse, secondStepResponse);

        // when
        ExecutorResult executorResult =
                recurringPaymentExecutor.executePayment(
                        agent, recurringPaymentRequest, credentials);

        // then
        Assertions.assertThat(executorResult.getPayment()).isEqualTo(payment);
    }

    @Test
    public void shouldThrowTransferAgentWorkerCommandExecutionException() throws PaymentException {
        // given
        RecurringPayment recurringPayment = createRecurringPayment();
        given(recurringPaymentRequest.getRecurringPayment()).willReturn(recurringPayment);

        willThrow(new PaymentException(InternalStatus.INVALID_DESTINATION_MESSAGE))
                .given(paymentController)
                .create(any());

        // when
        Throwable throwable =
                Assertions.catchThrowable(
                        () ->
                                recurringPaymentExecutor.executePayment(
                                        agent, recurringPaymentRequest, credentials));

        // then
        Assertions.assertThat(throwable)
                .isInstanceOf(TransferAgentWorkerCommandExecutionException.class);
    }

    private PaymentResponse mockPaymentResponse() {
        PaymentResponse paymentResponse = mock(PaymentResponse.class);
        given(paymentResponse.getStorage()).willReturn(mock(Storage.class));
        given(paymentResponse.getPayment()).willReturn(mock(Payment.class));
        return paymentResponse;
    }

    private PaymentMultiStepResponse mockFirstPaymentStepResponse() {
        PaymentMultiStepResponse paymentMultiStepResponse = mock(PaymentMultiStepResponse.class);
        given(paymentMultiStepResponse.getStep()).willReturn(AuthenticationStepConstants.STEP_INIT);
        given(paymentMultiStepResponse.getPayment()).willReturn(mock(Payment.class));
        given(paymentMultiStepResponse.getStorage()).willReturn(mock(Storage.class));
        return paymentMultiStepResponse;
    }

    private PaymentMultiStepResponse mockSecondPaymentStepResponse(Payment payment) {
        PaymentMultiStepResponse paymentMultiStepResponse = mock(PaymentMultiStepResponse.class);
        given(paymentMultiStepResponse.getStep())
                .willReturn(AuthenticationStepConstants.STEP_FINALIZE);
        given(paymentMultiStepResponse.getPayment()).willReturn(payment);
        return paymentMultiStepResponse;
    }

    private RecurringPayment createRecurringPayment() {
        AccountIdentifier accountIdentifier = new IbanIdentifier("AL35202111090000000001234567");
        RecurringPayment recurringPayment = new RecurringPayment();
        recurringPayment.setDestination(accountIdentifier);
        recurringPayment.setAmount(BigDecimal.ONE, "PLN");
        recurringPayment.setDueDate(Date.valueOf("2020-08-13"));
        return recurringPayment;
    }
}
