package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fidor.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class HrefEntity {
    private String href;

    public String getHref() {
        return href;
    }
}
