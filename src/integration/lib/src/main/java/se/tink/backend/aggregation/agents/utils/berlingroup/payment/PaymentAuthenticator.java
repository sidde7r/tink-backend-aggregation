package se.tink.backend.aggregation.agents.utils.berlingroup.payment;

import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.utils.berlingroup.common.LinksEntity;

public interface PaymentAuthenticator {

    public void authenticatePayment(Credentials credentials, LinksEntity scaLinks);
}
