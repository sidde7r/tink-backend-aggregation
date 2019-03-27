package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.fetcher.transactionalaccount.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LinksEntity {

    private LinksDetailsEntity account;

    private LinksDetailsEntity balances;

    private LinksDetailsEntity transactions;
}
