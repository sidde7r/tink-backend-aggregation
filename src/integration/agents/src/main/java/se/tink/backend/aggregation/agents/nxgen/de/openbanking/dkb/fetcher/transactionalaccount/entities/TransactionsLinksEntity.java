package se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.fetcher.transactionalaccount.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionsLinksEntity {
    private String account;
    private LinksDetailsEntity download;
    private String first;
    private String last;
    private LinksDetailsEntity next;
    private LinksDetailsEntity previous;
    private LinksDetailsEntity transactionDetails;
}
