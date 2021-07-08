package se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.authenticator;

import se.tink.backend.aggregation.agents.utils.berlingroup.payment.PaymentAuthenticator;

public interface PaymentAuthenticatorPreAuth extends PaymentAuthenticator {

    void preAuthentication();
}
