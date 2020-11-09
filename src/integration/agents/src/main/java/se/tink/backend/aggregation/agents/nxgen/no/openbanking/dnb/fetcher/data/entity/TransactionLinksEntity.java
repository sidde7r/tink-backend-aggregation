package se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.data.entity;

import se.tink.backend.aggregation.agents.Href;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionLinksEntity {

    private Href next;

    public String getNext() {
        return next != null ? next.getHref() : null;
    }
}
