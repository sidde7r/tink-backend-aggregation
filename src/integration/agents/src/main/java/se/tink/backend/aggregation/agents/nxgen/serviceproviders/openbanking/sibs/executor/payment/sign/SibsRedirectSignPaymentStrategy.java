package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment.sign;

import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.Retryer;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsConstants.Storage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment.entities.dictionary.SibsPaymentType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment.entities.dictionary.SibsTransactionStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.utils.SibsUtils;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepRequest;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.payment.rpc.Payment;

public class SibsRedirectSignPaymentStrategy extends AbstractSibsSignPaymentStrategy {

    protected final SibsRedirectCallbackHandler redirectCallbackHandler;

    private static final long SLEEP_TIME = 10L;
    private static final int RETRY_ATTEMPTS = 59;

    private final Retryer<SibsTransactionStatus> paymentStatusRetryer =
            SibsUtils.getPaymentStatusRetryer(SLEEP_TIME, RETRY_ATTEMPTS);

    public SibsRedirectSignPaymentStrategy(
            SibsBaseApiClient apiClient, SibsRedirectCallbackHandler redirectCallbackHandler) {
        super(apiClient);
        this.redirectCallbackHandler = redirectCallbackHandler;
    }

    @Override
    protected SibsTransactionStatus verifyStatusAfterSigning(
            PaymentMultiStepRequest paymentMultiStepRequest,
            SibsPaymentType paymentType,
            Payment payment)
            throws PaymentException {
        SibsTransactionStatus transactionStatus;
        try {
            transactionStatus =
                    paymentStatusRetryer.call(
                            () -> getCurrentStatus(paymentMultiStepRequest, paymentType));

        } catch (ExecutionException | RetryException e) {
            transactionStatus = getCurrentStatus(paymentMultiStepRequest, paymentType);
        }
        payment.setStatus(transactionStatus.getTinkStatus());
        checkStatusAfterSign(transactionStatus);
        return transactionStatus;
    }

    @Override
    protected void executeSignStrategy(
            PaymentMultiStepRequest paymentMultiStepRequest,
            SibsPaymentType paymentType,
            Payment payment)
            throws PaymentException {
        Optional<Map<String, String>> response =
                redirectCallbackHandler.handleRedirect(
                        new URL(
                                paymentMultiStepRequest
                                        .getStorage()
                                        .get(Storage.PAYMENT_REDIRECT_URI)),
                        paymentMultiStepRequest.getStorage().get(Storage.STATE));

        if (!response.isPresent()) {
            throw new PaymentException("SCA time-out.");
        }
    }
}
