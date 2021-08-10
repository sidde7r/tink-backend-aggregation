package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment.sign;

import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment.entities.dictionary.SibsPaymentType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment.entities.dictionary.SibsTransactionStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment.rpc.SibsGetPaymentStatusResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepConstants;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepResponse;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.rpc.Payment;

public abstract class AbstractSibsSignPaymentStrategy implements SignPaymentStrategy {

    protected final SibsBaseApiClient apiClient;

    protected AbstractSibsSignPaymentStrategy(SibsBaseApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public PaymentMultiStepResponse sign(
            PaymentMultiStepRequest paymentMultiStepRequest, SibsPaymentType paymentType)
            throws PaymentException {
        String nextStep;
        Payment payment = paymentMultiStepRequest.getPayment();
        switch (paymentMultiStepRequest.getStep()) {
            case AuthenticationStepConstants.STEP_INIT:
                executeSignStrategy(paymentMultiStepRequest, paymentType, payment);
                nextStep = SibsConstants.SibsSignSteps.SIBS_PAYMENT_POST_SIGN_STATE;
                break;
            case SibsConstants.SibsSignSteps.SIBS_PAYMENT_POST_SIGN_STATE:
                SibsTransactionStatus sibsTransactionStatus =
                        verifyStatusAfterSigning(paymentMultiStepRequest, paymentType, payment);
                payment.setStatus(sibsTransactionStatus.getTinkStatus());
                nextStep = AuthenticationStepConstants.STEP_FINALIZE;
                break;
            default:
                throw new IllegalStateException(
                        String.format(
                                "Unknown step %s for payment sign.",
                                paymentMultiStepRequest.getStep()));
        }

        return new PaymentMultiStepResponse(payment, nextStep);
    }

    protected abstract SibsTransactionStatus verifyStatusAfterSigning(
            PaymentMultiStepRequest paymentMultiStepRequest,
            SibsPaymentType paymentType,
            Payment payment)
            throws PaymentException;

    protected abstract void executeSignStrategy(
            PaymentMultiStepRequest paymentMultiStepRequest,
            SibsPaymentType paymentType,
            Payment payment)
            throws PaymentException;

    protected SibsTransactionStatus getCurrentStatus(
            PaymentMultiStepRequest paymentMultiStepRequest, SibsPaymentType paymentType) {
        SibsGetPaymentStatusResponse paymentStatusResponse =
                apiClient.getPaymentStatus(
                        paymentMultiStepRequest.getPayment().getUniqueId(), paymentType);
        return paymentStatusResponse.getTransactionStatus();
    }

    protected static void checkStatusAfterSign(SibsTransactionStatus transactionStatus)
            throws PaymentException {
        PaymentStatus tinkStatus = transactionStatus.getTinkStatus();
        if (PaymentStatus.PAID != tinkStatus) {
            throw new PaymentException(
                    "Unexpected payment status tink -> '"
                            + tinkStatus
                            + "' sibs -> '"
                            + transactionStatus
                            + "'");
        }
    }
}
