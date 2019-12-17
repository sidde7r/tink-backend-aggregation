package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.fetcher.entities;

import se.tink.backend.aggregation.agents.Href;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LinksEntity {
    private Href next;
    private Href self;

    public Href getNext() {
        return next;
    }

    public Href getSelf() {
        return self;
    }
}
