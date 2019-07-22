package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LinksEntity {
    private ScaEntity scaRedirect;

    public ScaEntity getScaRedirect() {
        return scaRedirect;
    }
}
