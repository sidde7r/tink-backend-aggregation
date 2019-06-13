package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.pis.config;

import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.pis.rpc.international.FundsConfirmationResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;

public interface UKPisConfig {
    PaymentResponse createPaymentConsent(PaymentRequest paymentRequest) throws PaymentException;

    PaymentResponse fetchPayment(PaymentRequest paymentRequest) throws PaymentException;

    FundsConfirmationResponse fetchFundsConfirmation(PaymentRequest paymentRequest)
            throws PaymentException;

    PaymentResponse executePayment(
            PaymentRequest paymentRequest,
            String endToEndIdentification,
            String instructionIdentification)
            throws PaymentException;
}
