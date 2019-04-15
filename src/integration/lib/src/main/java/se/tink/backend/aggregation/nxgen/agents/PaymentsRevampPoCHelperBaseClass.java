package se.tink.backend.aggregation.nxgen.agents;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.TransferExecutionException;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentListResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.libraries.credentials.service.CredentialsRequest;

import java.util.Optional;

// Idea is that all this should be part of NextGenerationAgent class
@Deprecated
public abstract class PaymentsRevampPoCHelperBaseClass extends NextGenerationAgent {
    private PaymentController paymentController;

    protected PaymentsRevampPoCHelperBaseClass(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
    }

    public abstract Optional<PaymentController> constructPaymentController();

    public PaymentResponse createPayment(PaymentRequest paymentRequest) {
        return getPaymentController().create(paymentRequest);
    }

    public PaymentResponse fetchPayment(PaymentRequest paymentRequest) {
        return getPaymentController().fetch(paymentRequest);
    }

    public PaymentMultiStepResponse signPayment(PaymentMultiStepRequest paymentRequest) {
        return getPaymentController().sign(paymentRequest);
    }

    public PaymentMultiStepResponse createBeneficiary() {
        return getPaymentController().createBeneficiary();
    }

    public PaymentResponse cancelPayment(PaymentRequest paymentRequest) {
        return getPaymentController().cancel(paymentRequest);
    }

    public PaymentListResponse fetchPayments(PaymentRequest paymentRequest) {
        return getPaymentController().fetchMultiple(paymentRequest);
    }

    private PaymentController getPaymentController() {
        if (paymentController == null) {
            paymentController = constructPaymentController().orElse(null);
        }

        Optional<PaymentController> paymentController = Optional.ofNullable(this.paymentController);
        TransferExecutionException.throwIf(!paymentController.isPresent());

        return paymentController.get();
    }
}
