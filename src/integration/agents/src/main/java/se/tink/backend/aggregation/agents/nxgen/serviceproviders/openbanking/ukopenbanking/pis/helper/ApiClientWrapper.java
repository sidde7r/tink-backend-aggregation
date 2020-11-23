package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.helper;

import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.dto.common.FundsConfirmationResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;

public interface ApiClientWrapper {

    PaymentResponse createPaymentConsent(PaymentRequest paymentRequest);

    PaymentResponse getPayment(String paymentId);

    PaymentResponse getPaymentConsent(String consentId);

    Optional<FundsConfirmationResponse> getFundsConfirmation(String consentId);

    PaymentResponse executePayment(
            PaymentRequest paymentRequest,
            String consentId,
            String endToEndIdentification,
            String instructionIdentification);
}
