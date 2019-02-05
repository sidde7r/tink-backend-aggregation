package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PaginationEntity {
    private int end;
    private String lastPage;
    private String nextPage;
    private int numPages;
    private int page;
    private String firstPage;
    private int total;
    private int start;

    public int getEnd() {
        return end;
    }

    public String getLastPage() {
        return lastPage;
    }

    public String getNextPage() {
        return nextPage;
    }

    public int getNumPages() {
        return numPages;
    }

    public int getPage() {
        return page;
    }

    public String getFirstPage() {
        return firstPage;
    }

    public int getTotal() {
        return total;
    }

    public int getStart() {
        return start;
    }
}
