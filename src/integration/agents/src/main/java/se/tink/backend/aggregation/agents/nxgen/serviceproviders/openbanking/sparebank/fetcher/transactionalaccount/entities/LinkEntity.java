package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.fetcher.transactionalaccount.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LinkEntity {
    private String href;

    public LinkEntity() {}

    public String getHref() {
        return href;
    }

    public boolean hasNextLink() {
        return href != null;
    }
}
