package se.tink.backend.aggregation.agents.nxgen.se.openbanking.corporate.seb.fetcher.transactionalaccount.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LinksDetailsEntity {
    public String href;

    public String getHref() {
        return href;
    }
}
