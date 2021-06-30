package se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.pis.signer;

import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationException;

public interface DemobankPaymentSigner {
    void sign() throws PaymentAuthorizationException;
}
