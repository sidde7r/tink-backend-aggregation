package se.tink.backend.aggregation.agents.nxgen.hu.openbanking.raiffeisen.fetcher.transactionalaccount.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionsLinksEntity {

    private String currentPage;
    private String firstPage;
    private String lastPage;
    private String nextPage;
    private String secondPage;
    private String viewAccount;

    public Boolean canFetchMore() {
        return !currentPage.equalsIgnoreCase(lastPage);
    }
}
