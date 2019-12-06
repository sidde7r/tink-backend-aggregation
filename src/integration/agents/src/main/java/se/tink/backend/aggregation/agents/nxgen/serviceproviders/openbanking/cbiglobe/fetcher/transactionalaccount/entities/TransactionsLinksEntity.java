package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.fetcher.transactionalaccount.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionsLinksEntity {
    private TransactionsLinkDetailsEntity account;
    private TransactionsLinkDetailsEntity transactionDetails;
}
