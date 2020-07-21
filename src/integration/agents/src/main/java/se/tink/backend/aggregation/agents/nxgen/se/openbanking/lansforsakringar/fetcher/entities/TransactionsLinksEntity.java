package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.fetcher.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionsLinksEntity {

    private LinksDetailsEntity first;
    private LinksDetailsEntity next;

    public LinksDetailsEntity getFirst() {
        return first;
    }

    public LinksDetailsEntity getNext() {
        return next;
    }

    public boolean hasNext() {
        return next != null;
    }
}
