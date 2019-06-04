package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.entity;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class Href {
    private String href;

    public Href() {}

    public String getHref() {
        return href;
    }
}
