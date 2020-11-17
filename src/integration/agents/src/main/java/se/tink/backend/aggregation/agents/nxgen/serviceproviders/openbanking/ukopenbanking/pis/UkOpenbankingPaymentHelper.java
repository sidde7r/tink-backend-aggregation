package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis;

import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;

public interface UkOpenbankingPaymentHelper {

    PaymentResponse createConsent(PaymentRequest paymentRequest);

    PaymentResponse fetchPaymentIfAlreadyExecutedOrGetConsent(PaymentRequest paymentRequest);

    PaymentResponse executePayment(
            PaymentRequest paymentRequest,
            String endToEndIdentification,
            String instructionIdentification);
}
