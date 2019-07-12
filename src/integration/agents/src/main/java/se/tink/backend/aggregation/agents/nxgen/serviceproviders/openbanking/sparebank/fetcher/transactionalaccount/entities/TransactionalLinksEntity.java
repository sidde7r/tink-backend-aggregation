package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.fetcher.transactionalaccount.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionalLinksEntity {
    private LinkEntity next;

    public boolean hasNextLink() {
        return next != null && next.hasNextLink();
    }
}
