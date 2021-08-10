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
import se.tink.backend.aggregation.agents.TypedPaymentControllerable;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentRejectedException;
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
public class TypedRecurringPaymentExecutorTest {

    @Mock private Credentials credentials;

    @Mock private TypedPaymentControllerable agent;

    @Mock private PaymentController paymentController;

    @Mock private RecurringPaymentRequest recurringPaymentRequest;
    @Mock private PaymentResponse paymentResponse;

    Payment payment = new Payment.Builder().build();
    private TypedRecurringPaymentExecutor typedRecurringPaymentExecutor;

    @Before
    public void setUp() throws PaymentRejectedException {
        typedRecurringPaymentExecutor = new TypedRecurringPaymentExecutor(null);
        given(agent.getPaymentController(any())).willReturn(Optional.of(paymentController));
        given(recurringPaymentRequest.getRecurringPayment()).willReturn(createRecurringPayment());
    }

    @Test
    public void shouldHandlePayment() {
        // when
        boolean result =
                typedRecurringPaymentExecutor.canHandlePayment(agent, recurringPaymentRequest);

        // then
        Assert.assertTrue(result);
    }

    @Test
    public void testExecute() {

        try {
            given(paymentResponse.getStorage()).willReturn(new Storage());
            given(paymentResponse.getPayment()).willReturn(new Payment.Builder().build());
            given(paymentController.create(any())).willReturn(paymentResponse);

            PaymentMultiStepResponse secondStepResponse = mockSecondStepResponse(payment);

            given(paymentController.sign(any())).willReturn(secondStepResponse);

            // when
            ExecutorResult executorResult =
                    typedRecurringPaymentExecutor.executePayment(
                            agent, recurringPaymentRequest, credentials);

            // then
            Assertions.assertThat(executorResult.getPayment()).isEqualTo(payment);
        } catch (Exception e) {
            Assert.fail();
        }
    }

    @Test
    public void shouldThrowTransferAgentWorkerCommandExecutionException() {
        // given
        try {
            willThrow(new PaymentException(InternalStatus.INVALID_DESTINATION_MESSAGE))
                    .given(paymentController)
                    .create(any());
        } catch (PaymentException e) {
            Assert.fail();
        }

        // when
        Throwable throwable =
                Assertions.catchThrowable(
                        () ->
                                typedRecurringPaymentExecutor.executePayment(
                                        agent, recurringPaymentRequest, credentials));

        // then
        Assertions.assertThat(throwable)
                .isInstanceOf(TransferAgentWorkerCommandExecutionException.class);
    }

    private RecurringPayment createRecurringPayment() {
        AccountIdentifier accountIdentifier = new IbanIdentifier("AL35202111090000000001234567");
        RecurringPayment recurringPayment = new RecurringPayment();
        recurringPayment.setDestination(accountIdentifier);
        recurringPayment.setAmount(BigDecimal.ONE, "PLN");
        recurringPayment.setDueDate(Date.valueOf("2020-08-13"));
        return recurringPayment;
    }

    private PaymentMultiStepResponse mockSecondStepResponse(Payment payment) {
        PaymentMultiStepResponse response = mock(PaymentMultiStepResponse.class);
        given(response.getPayment()).willReturn(payment);
        given(response.getStep()).willReturn(AuthenticationStepConstants.STEP_FINALIZE);
        return response;
    }
}
