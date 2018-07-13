package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LinkEntity {
    private String rel;
    private String href;

    public String getRel() {
        return rel;
    }

    public String getHref() {
        return href;
    }
}
