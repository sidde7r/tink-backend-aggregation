package se.tink.backend.aggregation.agents.utils.berlingroup.payment;

import se.tink.backend.aggregation.agents.utils.berlingroup.common.LinksEntity;

public interface PaymentAuthenticator {

    void authenticatePayment(LinksEntity scaLinks);
}
