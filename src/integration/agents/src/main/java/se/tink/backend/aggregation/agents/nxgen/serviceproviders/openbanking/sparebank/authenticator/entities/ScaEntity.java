package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ScaEntity {
    private String href;

    public String getHref() {
        return href;
    }
}
