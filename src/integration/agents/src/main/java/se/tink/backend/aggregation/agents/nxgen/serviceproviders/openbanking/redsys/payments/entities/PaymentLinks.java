package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.payments.entities;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.entities.LinkEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PaymentLinks {
    private LinkEntity scaRedirect;

    public String getScaRedirectLink() {
        return scaRedirect.getHref();
    }
}
