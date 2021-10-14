package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment.sign;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationTimeOutException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentCancelledException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentRejectedException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment.entities.dictionary.SibsTransactionStatus;

@RunWith(JUnitParamsRunner.class)
public class SibsRedirectSignPaymentStrategyTest {

    private final SibsBaseApiClient apiClient = mock(SibsBaseApiClient.class);
    private final SibsRedirectCallbackHandler redirectCallbackHandler =
            mock(SibsRedirectCallbackHandler.class);
    private final SibsRedirectSignPaymentStrategy sibsRedirectSignPaymentStrategy =
            new SibsRedirectSignPaymentStrategy(apiClient, redirectCallbackHandler);

    @Test
    @Parameters({"ACTC", "ACCP", "ACFC", "ACWC", "ACSP", "ACSC", "ACCC"})
    public void shouldInterpretStatusAsPaidInTinkContext(
            SibsTransactionStatus sibsTransactionStatus) throws PaymentException {
        sibsRedirectSignPaymentStrategy.validateStatusAfterSign(sibsTransactionStatus);
    }

    @Test
    @Parameters({"RCVD", "PDNG", "PATC"})
    public void shouldThrowOnPendingStatuses(SibsTransactionStatus sibsTransactionStatus) {
        assertThatThrownBy(
                        () ->
                                sibsRedirectSignPaymentStrategy.validateStatusAfterSign(
                                        sibsTransactionStatus))
                .isInstanceOf(PaymentAuthorizationTimeOutException.class);
    }

    @Test
    @Parameters({"RJC", "RJCT"})
    public void shouldThrowOnRejectedStatuses(SibsTransactionStatus sibsTransactionStatus) {
        assertThatThrownBy(
                        () ->
                                sibsRedirectSignPaymentStrategy.validateStatusAfterSign(
                                        sibsTransactionStatus))
                .isInstanceOf(PaymentRejectedException.class);
    }

    @Test
    public void shouldThrowOnCancelledStatus() {
        assertThatThrownBy(
                        () ->
                                sibsRedirectSignPaymentStrategy.validateStatusAfterSign(
                                        SibsTransactionStatus.CANC))
                .isInstanceOf(PaymentCancelledException.class);
    }
}
