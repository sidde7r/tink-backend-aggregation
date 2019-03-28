package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.akita.fetcher.transactionalaccount.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionsLinksEntity {

    private String account;
    private String first;
    private String next;
    private String transactionDetails;
}
