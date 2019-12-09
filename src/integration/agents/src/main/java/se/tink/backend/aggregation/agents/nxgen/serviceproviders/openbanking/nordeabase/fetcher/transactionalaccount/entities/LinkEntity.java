package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.fetcher.transactionalaccount.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LinkEntity {

    private String href;
    private String rel;

    public String getHref() {
        return href;
    }

    public String getRel() {
        return rel;
    }
}
