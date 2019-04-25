package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.pis.config;

import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.pis.rpc.international.FundsConfirmationResponse;
import se.tink.libraries.payment.rpc.Payment;

public interface UKPisConfig {
    Payment createPaymentConsent(Payment payment) throws PaymentException;

    Payment fetchPayment(Payment payment) throws PaymentException;

    FundsConfirmationResponse fetchFundsConfirmation(Payment payment)
            throws PaymentException;

    Payment executePayment(
            Payment payment,
            String endToEndIdentification,
            String instructionIdentification)
            throws PaymentException;
}
