package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ScaOAuthEntity {
    private String href;

    public String getHref() {
        return href;
    }
}
