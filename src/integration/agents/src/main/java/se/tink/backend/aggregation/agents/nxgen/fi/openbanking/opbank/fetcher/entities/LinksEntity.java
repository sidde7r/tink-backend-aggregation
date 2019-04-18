package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.fetcher.entities;


import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LinksEntity {
    private HrefLinkEntity next;
    private HrefLinkEntity self;

    public HrefLinkEntity getNext() {
        return next;
    }

    public HrefLinkEntity getSelf() {
        return self;
    }
}
