package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LinkEntity {
    private String href;

    public String getLink() {
        return href;
    }
}
