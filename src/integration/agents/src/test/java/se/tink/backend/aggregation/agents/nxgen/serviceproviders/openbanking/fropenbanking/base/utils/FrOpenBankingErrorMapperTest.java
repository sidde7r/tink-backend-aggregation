package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.utils;

import static junit.framework.TestCase.assertEquals;

import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.payment.InsufficientFundsException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationCancelledByUserException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.libraries.signableoperation.enums.InternalStatus;

public class FrOpenBankingErrorMapperTest {

    @Test
    public void testIncorrectAccountNumberException() {

        // when
        PaymentException paymentException = FrOpenBankingErrorMapper.mapToError("AC01");

        // then
        assertEquals(
                InternalStatus.INVALID_SOURCE_ACCOUNT.toString(),
                paymentException.getInternalStatus());
        assertEquals("Incorrect account number.", paymentException.getMessage());
    }

    @Test
    public void testAccountIsClosedException() {

        // when
        PaymentException paymentException = FrOpenBankingErrorMapper.mapToError("AC04");

        // then
        assertEquals(
                InternalStatus.INVALID_SOURCE_ACCOUNT.toString(),
                paymentException.getInternalStatus());
        assertEquals("Account is closed.", paymentException.getMessage());
    }

    @Test
    public void testTransactionIsForbidden() {

        // when
        PaymentException paymentException = FrOpenBankingErrorMapper.mapToError("AG01");

        // then
        assertEquals(
                InternalStatus.INVALID_ACCOUNT_TYPE_COMBINATION.toString(),
                paymentException.getInternalStatus());
        assertEquals("Transaction is forbidden.", paymentException.getMessage());
    }

    @Test
    public void testTooManyTransactions() {

        // when
        PaymentException paymentException = FrOpenBankingErrorMapper.mapToError("AM18");

        // then
        assertEquals(
                InternalStatus.TRANSFER_LIMIT_REACHED.toString(),
                paymentException.getInternalStatus());
        assertEquals(
                "The number of transactions exceeds the acceptance limit.",
                paymentException.getMessage());
    }

    @Test
    public void testAccountIsBlockedException() {

        // when
        PaymentException paymentException = FrOpenBankingErrorMapper.mapToError("AC06");

        // then
        assertEquals(
                InternalStatus.INVALID_SOURCE_ACCOUNT.toString(),
                paymentException.getInternalStatus());
        assertEquals("Account is blocked.", paymentException.getMessage());
    }

    @Test
    public void testPaymentDateTooFarException() {

        // when
        PaymentException paymentException = FrOpenBankingErrorMapper.mapToError("CH03");

        // then
        assertEquals(
                "The requested payment date is too far in the future.",
                paymentException.getMessage());
    }

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
    public void testFraudulentPaymentException() {

        // when
        PaymentException paymentException = FrOpenBankingErrorMapper.mapToError("FRAD");

        // then
        assertEquals(
                InternalStatus.FRAUDULENT_PAYMENT.toString(), paymentException.getInternalStatus());

        assertEquals(
                "The Payment Request is considered as fraudulent.", paymentException.getMessage());
    }

    @Test
    public void testRejectedDueToRegulatoryReasons() {

        // when
        PaymentException paymentException = FrOpenBankingErrorMapper.mapToError("RR04");

        // then
        assertEquals(
                InternalStatus.INVALID_ACCOUNT_TYPE_COMBINATION.toString(),
                paymentException.getInternalStatus());

        assertEquals("Rejected due to regulatory reasons.", paymentException.getMessage());
    }

    @Test
    public void testPaymentAuthorizationTimeOutException() {

        // when
        PaymentException paymentException = FrOpenBankingErrorMapper.mapToError("NOAS");

        // then
        assertEquals(
                InternalStatus.PAYMENT_AUTHORIZATION_TIMEOUT.toString(),
                paymentException.getInternalStatus());

        assertEquals(
                "Authorisation of payment timed out. Please try again.",
                paymentException.getMessage());
    }

    @Test
    public void testInvalidAccount() {

        // when
        PaymentException paymentException = FrOpenBankingErrorMapper.mapToError("RR01");

        // then
        assertEquals(
                InternalStatus.INVALID_SOURCE_ACCOUNT.toString(),
                paymentException.getInternalStatus());

        assertEquals(
                "Could not validate the account, you are trying to pay from.",
                paymentException.getMessage());
    }

    @Test
    public void testMissingCreditorNameOrAddress() {

        // when
        PaymentException paymentException = FrOpenBankingErrorMapper.mapToError("RR03");

        // then
        assertEquals(
                InternalStatus.INVALID_DESTINATION_ACCOUNT.toString(),
                paymentException.getInternalStatus());

        assertEquals("Missing creditor name or address.", paymentException.getMessage());
    }

    @Test
    public void testPaymentRejectedException() {

        // when
        PaymentException paymentException = FrOpenBankingErrorMapper.mapToError("MS03");

        // then
        assertEquals(
                InternalStatus.INVALID_ACCOUNT_TYPE_COMBINATION.toString(),
                paymentException.getInternalStatus());

        assertEquals("Rejected due to regulatory reasons.", paymentException.getMessage());
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
