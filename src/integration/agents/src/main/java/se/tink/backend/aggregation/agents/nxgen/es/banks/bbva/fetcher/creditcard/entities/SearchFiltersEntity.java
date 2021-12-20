package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.creditcard.entities;

import static se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaConstants.PostParameter.SEARCH_TEXT;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SearchFiltersEntity {

    private CardAmountEntity amount;
    private String searchText;
    private TransactionDateEntity transactionDate;

    public CardAmountEntity getAmount() {
        return amount;
    }

    public String getSearchText() {
        return searchText;
    }

    public TransactionDateEntity getTransactionDate() {
        return transactionDate;
    }

    public SearchFiltersEntity(
            CardAmountEntity amount, String searchText, TransactionDateEntity transactionDate) {
        this.amount = amount;
        this.searchText = searchText;
        this.transactionDate = transactionDate;
    }

    public SearchFiltersEntity(TransactionDateEntity transactionDate) {
        this.transactionDate = transactionDate;
        this.searchText = SEARCH_TEXT;
        this.amount = new CardAmountEntity();
    }
}
