package se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.pis.apiclient;

import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.libraries.payment.enums.PaymentStatus;

public interface DemobankPaymentApiClient {
    PaymentResponse createPayment(PaymentRequest paymentRequest) throws PaymentException;

    OAuth2Token exchangeAccessCode(String code);

    PaymentResponse getPayment(String paymentId);

    PaymentStatus getPaymentStatus(String paymentId);
}
