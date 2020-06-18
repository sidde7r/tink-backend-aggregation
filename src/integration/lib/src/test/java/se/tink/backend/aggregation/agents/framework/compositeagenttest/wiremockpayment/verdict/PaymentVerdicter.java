package se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.verdict;

import org.junit.Assert;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepResponse;
import se.tink.libraries.payment.enums.PaymentStatus;

public class PaymentVerdicter implements PaymentVerdict {

    @Override
    public void verdictOnPaymentStatus(PaymentMultiStepResponse signPaymentMultiStepResponse) {
        PaymentStatus statusResult = signPaymentMultiStepResponse.getPayment().getStatus();
        Assert.assertTrue(
                statusResult.equals(PaymentStatus.SIGNED)
                        || statusResult.equals(PaymentStatus.PAID));
    }
}
