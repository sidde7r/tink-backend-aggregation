package se.tink.backend.aggregation.agents.nxgen.de.openbanking.commerzbank.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ScaStatusEntity {

    private String href;

    public String getHref() {
        return href;
    }
}
