package se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.verdict;

import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepResponse;

public interface PaymentVerdict {
    void verdictOnPaymentStatus(PaymentMultiStepResponse signPaymentMultiStepResponse)
            throws PaymentException;
}
