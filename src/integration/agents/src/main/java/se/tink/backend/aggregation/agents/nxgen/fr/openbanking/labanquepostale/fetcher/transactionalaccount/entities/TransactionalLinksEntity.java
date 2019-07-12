package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.fetcher.transactionalaccount.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionalLinksEntity {
    private NextLinkEntity next;

    public boolean hasNextLink() {
        return next != null && next.hasNextLink();
    }

    public String getNextLink() {
        return next.getsNextLink();
    }
}
