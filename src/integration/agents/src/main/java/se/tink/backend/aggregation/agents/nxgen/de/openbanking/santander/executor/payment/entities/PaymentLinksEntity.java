package se.tink.backend.aggregation.agents.nxgen.de.openbanking.santander.executor.payment.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PaymentLinksEntity {

    private LinkEntity scaOAuth;
    private LinkEntity self;
    private LinkEntity status;

    public String getSelf() {
        return self.getHref();
    }
}
