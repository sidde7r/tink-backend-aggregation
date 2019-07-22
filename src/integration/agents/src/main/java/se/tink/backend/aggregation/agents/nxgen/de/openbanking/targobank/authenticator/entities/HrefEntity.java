package se.tink.backend.aggregation.agents.nxgen.de.openbanking.targobank.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class HrefEntity {
    private String href;

    public String getLink() {
        return href;
    }
}
