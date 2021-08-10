package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.utils;

import static junit.framework.TestCase.assertEquals;

import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.payment.InsufficientFundsException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationCancelledByUserException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentRejectedException;
import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException.EndUserMessage;
import se.tink.libraries.signableoperation.enums.InternalStatus;

public class FrOpenBankingErrorMapperTest {

    @Test
    public void testInsufficientFundsException() {

        // when
        PaymentException paymentException = FrOpenBankingErrorMapper.mapToError("CUST");

        // then
        assertEquals(
                InternalStatus.INSUFFICIENT_FUNDS.toString(), paymentException.getInternalStatus());

        assertEquals(InsufficientFundsException.DEFAULT_MESSAGE, paymentException.getMessage());
    }

    @Test
    public void testInvalidSourceAccountException() {

        // when
        PaymentException paymentException = FrOpenBankingErrorMapper.mapToError("AG01");

        // then
        assertEquals(
                InternalStatus.INVALID_SOURCE_ACCOUNT.toString(),
                paymentException.getInternalStatus());

        assertEquals(EndUserMessage.INVALID_SOURCE.getKey().get(), paymentException.getMessage());
    }

    @Test
    public void testPaymentCancelledException() {

        // when
        PaymentException paymentException = FrOpenBankingErrorMapper.mapToError("DS02");

        // then
        assertEquals(
                InternalStatus.PAYMENT_AUTHORIZATION_CANCELLED.toString(),
                paymentException.getInternalStatus());

        assertEquals(
                PaymentAuthorizationCancelledByUserException.MESSAGE,
                paymentException.getMessage());
    }

    @Test
    public void testPaymentRejectedException() {

        // when
        PaymentException paymentException = FrOpenBankingErrorMapper.mapToError("MS03");

        // then
        assertEquals(
                InternalStatus.PAYMENT_REJECTED_BY_BANK_NO_DESCRIPTION.toString(),
                paymentException.getInternalStatus());

        assertEquals(PaymentRejectedException.MESSAGE, paymentException.getMessage());
    }

    @Test
    public void testPaymentRejectedNoDescriptionException() {

        // when
        PaymentException paymentException = FrOpenBankingErrorMapper.mapToError("Something Else");

        // then
        assertEquals(
                InternalStatus.BANK_ERROR_CODE_NOT_HANDLED_YET.toString(),
                paymentException.getInternalStatus());

        assertEquals("Payment failed.", paymentException.getMessage());
    }
}
