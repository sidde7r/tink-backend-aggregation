package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.common;

import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;

public interface UkOpenBankingPaymentApiClient {

    PaymentResponse createPaymentConsent(PaymentRequest paymentRequest) throws PaymentException;

    PaymentResponse getPayment(String paymentId);

    PaymentResponse getPaymentConsent(String consentId);

    PaymentResponse executePayment(
            PaymentRequest paymentRequest,
            String consentId,
            String endToEndIdentification,
            String instructionIdentification)
            throws PaymentException;
}
