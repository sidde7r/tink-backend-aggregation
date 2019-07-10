package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fidor.fetcher.transactionalaccount.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LinkEntity {
    private String href;

    public String getHref() {
        return href;
    }

    public boolean hasNext() {
        return href != null;
    }
}
