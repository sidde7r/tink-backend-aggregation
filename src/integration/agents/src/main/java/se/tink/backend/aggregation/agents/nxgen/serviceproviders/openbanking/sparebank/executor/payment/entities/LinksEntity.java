package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.executor.payment.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LinksEntity {
    private ScaRedirectEntity scaRedirect;

    public String getScaRedirectEntity() {
        return scaRedirect.getScaRedirectLink();
    }
}
