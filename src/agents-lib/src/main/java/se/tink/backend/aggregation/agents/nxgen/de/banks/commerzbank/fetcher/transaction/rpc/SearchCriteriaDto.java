package se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.fetcher.transaction.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SearchCriteriaDto {
    private String fromDate;
    private String toDate;
    private int page;
    private String amountType;
    private int transactionsPerPage;
    private String text;

    public SearchCriteriaDto(String fromDate, String toDate, int page, String amountType,
            int transactionsPerPage, String text) {
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.page = page;
        this.amountType = amountType;
        this.transactionsPerPage = transactionsPerPage;
        this.text = text;
    }
}
