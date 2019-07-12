package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LinkEntity {

    private ScaRedirectEntity scaRedirect;

    public ScaRedirectEntity getScaRedirect() {
        return scaRedirect;
    }
}
