package se.tink.backend.aggregation.agents.nxgen.ro.banks.raiffeisen.fetcher.entity;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionLinksEntity {

    private String viewAccount;
    private String firstPage;
    private String secondPage;
    private String currentPage;
    private String nextPage;
    private String lastPage;

    public String getViewAccount() {
        return viewAccount;
    }

    public String getFirstPage() {
        return firstPage;
    }

    public String getSecondPage() {
        return secondPage;
    }

    public String getCurrentPage() {
        return currentPage;
    }

    public String getNextPage() {
        return nextPage;
    }

    public String getLastPage() {
        return lastPage;
    }


}
