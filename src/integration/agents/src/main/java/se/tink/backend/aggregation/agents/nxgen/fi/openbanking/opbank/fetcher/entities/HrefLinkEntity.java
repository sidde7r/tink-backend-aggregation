package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.fetcher.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class HrefLinkEntity {
    private String href;

    public String getHref() {
        return href;
    }
}
