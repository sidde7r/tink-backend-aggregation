package se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.verdict;

import org.junit.Assert;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.libraries.payment.enums.PaymentStatus;

public class GBPaymentVerdicter implements PaymentVerdict {

    private final PaymentController paymentController;

    public GBPaymentVerdicter(PaymentController paymentController) {
        this.paymentController = paymentController;
    }

    @Override
    public void verdictOnPaymentStatus(PaymentMultiStepResponse signPaymentMultiStepResponse)
            throws PaymentException {
        PaymentResponse paymentResponse =
                paymentController.fetch(PaymentMultiStepRequest.of(signPaymentMultiStepResponse));
        PaymentStatus statusResult = paymentResponse.getPayment().getStatus();
        Assert.assertTrue(
                statusResult.equals(PaymentStatus.SIGNED)
                        || statusResult.equals(PaymentStatus.PAID));
    }
}
