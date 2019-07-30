package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment.sign;

import java.util.Map;
import java.util.Optional;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsConstants.Storage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment.entities.dictionary.SibsPaymentType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment.entities.dictionary.SibsTransactionStatus;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepRequest;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.libraries.payment.rpc.Payment;

public class SibsRedirectSignPaymentStrategy extends AbstractSibsSignPaymentStrategy {

    protected final SibsRedirectCallbackHandler redirectCallbackHandler;

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
        SibsTransactionStatus transactionStatus =
                getCurrentStatus(paymentMultiStepRequest, paymentType);
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
