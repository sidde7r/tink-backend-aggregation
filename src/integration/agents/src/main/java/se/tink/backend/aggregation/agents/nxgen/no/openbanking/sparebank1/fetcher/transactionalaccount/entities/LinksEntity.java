package se.tink.backend.aggregation.agents.nxgen.no.openbanking.sparebank1.fetcher.transactionalaccount.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LinksEntity {

    private LinksDetailsEntity self;

    private LinksDetailsEntity transactions;

    private LinksDetailsEntity details;
}
