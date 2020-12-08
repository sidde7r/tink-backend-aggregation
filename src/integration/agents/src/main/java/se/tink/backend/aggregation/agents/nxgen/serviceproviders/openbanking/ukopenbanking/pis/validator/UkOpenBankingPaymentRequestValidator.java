package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.validator;

import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;

public interface UkOpenBankingPaymentRequestValidator {

    void validate(PaymentRequest paymentRequest);
}
