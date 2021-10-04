package se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.payment;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.aggregation.agents.bankid.status.BankIdStatus;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.executor.payment.NordeaSeBankIdSigner;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.executor.payment.NordeaSePaymentExecutorSelector;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.rpc.Payment;

@RunWith(JUnitParamsRunner.class)
public class NordeaSeBankIdSignerTest {

    private NordeaSePaymentExecutorSelector paymentExecutor;
    private NordeaSeBankIdSigner nordeaSeBankIdSigner;

    @Before
    public void setUp() throws Exception {
        paymentExecutor = mock(NordeaSePaymentExecutorSelector.class);
        nordeaSeBankIdSigner = new NordeaSeBankIdSigner(paymentExecutor);
    }

    @Test
    public void shouldThrowPaymentException() throws PaymentException {
        // given
        PaymentRequest paymentRequest = mock(PaymentRequest.class);
        PaymentException paymentException = mock(PaymentException.class);

        // when
        when(paymentExecutor.fetch(any())).thenThrow(paymentException);

        // then
        assertThatThrownBy(() -> nordeaSeBankIdSigner.collect(paymentRequest))
                .isInstanceOf(BankIdError.UNKNOWN.exception().getClass());
    }

    @Test
    @Parameters(method = "getParameters")
    public void shouldReturnProperStatusBasedOnGivenPaymentResponse(
            PaymentStatus paymentStatus, BankIdStatus bankIdStatus) throws PaymentException {
        // given
        PaymentRequest paymentRequest = mock(PaymentRequest.class);
        Payment payment = mock(Payment.class);
        PaymentResponse paymentResponse = mock(PaymentResponse.class);

        // when
        when(paymentExecutor.fetch(paymentRequest)).thenReturn(paymentResponse);
        when(paymentResponse.getPayment()).thenReturn(payment);
        when(payment.getStatus()).thenReturn(paymentStatus);

        // then
        assertEquals(nordeaSeBankIdSigner.collect(paymentRequest), bankIdStatus);
    }

    private Object[] getParameters() {
        return new Object[] {
            new Object[] {PaymentStatus.CREATED, BankIdStatus.WAITING},
            new Object[] {PaymentStatus.PENDING, BankIdStatus.WAITING},
            new Object[] {PaymentStatus.SIGNED, BankIdStatus.DONE},
            new Object[] {PaymentStatus.PAID, BankIdStatus.DONE},
            new Object[] {PaymentStatus.REJECTED, BankIdStatus.DONE},
            new Object[] {PaymentStatus.CANCELLED, BankIdStatus.DONE},
            new Object[] {PaymentStatus.USER_APPROVAL_FAILED, BankIdStatus.INTERRUPTED},
            new Object[] {PaymentStatus.UNDEFINED, BankIdStatus.FAILED_UNKNOWN}
        };
    }
}
