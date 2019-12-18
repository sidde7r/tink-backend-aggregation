package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment.sign;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment.entities.dictionary.SibsTransactionStatus;

@RunWith(JUnitParamsRunner.class)
public class SibsRedirectSignPaymentStrategyTest {

    private Object[] sibsPaidStatuses() {
        return new Object[] {
            SibsTransactionStatus.ACTC,
            SibsTransactionStatus.ACCP,
            SibsTransactionStatus.ACFC,
            SibsTransactionStatus.ACWC,
            SibsTransactionStatus.ACSP,
            SibsTransactionStatus.ACSC,
            SibsTransactionStatus.ACCC
        };
    }

    private Object[] sibsPendingOrRejectedStatuses() {
        return new Object[] {
            SibsTransactionStatus.RCVD,
            SibsTransactionStatus.PDNG,
            SibsTransactionStatus.PATC,
            SibsTransactionStatus.RJC,
            SibsTransactionStatus.CANC
        };
    }

    @Test
    @Parameters(method = "sibsPaidStatuses")
    public void shouldInterpretStatusAsPaidInTinkContext(
            SibsTransactionStatus sibsTransactionStatus) throws PaymentException {
        SibsRedirectSignPaymentStrategy.checkStatusAfterSign(sibsTransactionStatus);
    }

    @Test(expected = PaymentException.class)
    @Parameters(method = "sibsPendingOrRejectedStatuses")
    public void shouldInterpretStatusAsPendingPaidInTinkContext(
            SibsTransactionStatus sibsTransactionStatus) throws PaymentException {
        SibsRedirectSignPaymentStrategy.checkStatusAfterSign(sibsTransactionStatus);
    }
}
