package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.fetcher.transactionalaccount.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LinksEntity {
    private LinkEntity next;

    public boolean hasNextLink() {
        return next != null && next.hasNextLink();
    }

    public LinkEntity getNext() {
        return next;
    }
}
