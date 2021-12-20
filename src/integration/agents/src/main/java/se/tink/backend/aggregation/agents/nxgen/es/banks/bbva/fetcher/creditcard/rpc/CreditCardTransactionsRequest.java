package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.creditcard.rpc;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.creditcard.entities.CardEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.creditcard.entities.SearchFiltersEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CreditCardTransactionsRequest {

    private List<CardEntity> cards;
    private String customerId;
    private SearchFiltersEntity searchFilters;
    private Boolean showContractTransactions;
    private String sortedBy;
    private String sortedType;

    public List<CardEntity> getCards() {
        return cards;
    }

    public String getCustomerId() {
        return customerId;
    }

    public SearchFiltersEntity getSearchFilters() {
        return searchFilters;
    }

    public Boolean getShowContractTransactions() {
        return showContractTransactions;
    }

    public String getSortedBy() {
        return sortedBy;
    }

    public String getSortedType() {
        return sortedType;
    }

    public static CreditCardTransactionsRequestBuilder builder() {
        return new CreditCardTransactionsRequestBuilder();
    }

    public static class CreditCardTransactionsRequestBuilder {

        private List<CardEntity> cards;
        private String customerId;
        private SearchFiltersEntity searchFilters;
        private Boolean showContractTransactions;
        private String sortedBy;
        private String sortedType;

        public CreditCardTransactionsRequest.CreditCardTransactionsRequestBuilder withCards(
                List<CardEntity> cards) {
            this.cards = cards;
            return this;
        }

        public CreditCardTransactionsRequest.CreditCardTransactionsRequestBuilder withCustomerId(
                String customerId) {
            this.customerId = customerId;
            return this;
        }

        public CreditCardTransactionsRequest.CreditCardTransactionsRequestBuilder withSearchFilters(
                SearchFiltersEntity searchFilters) {
            this.searchFilters = searchFilters;
            return this;
        }

        public CreditCardTransactionsRequest.CreditCardTransactionsRequestBuilder
                withShowContractTransactions(Boolean showContractTransactions) {
            this.showContractTransactions = showContractTransactions;
            return this;
        }

        public CreditCardTransactionsRequest.CreditCardTransactionsRequestBuilder withSortedBy(
                String sortedBy) {
            this.sortedBy = sortedBy;
            return this;
        }

        public CreditCardTransactionsRequest.CreditCardTransactionsRequestBuilder withSortedType(
                String sortedType) {
            this.sortedType = sortedType;
            return this;
        }

        public CreditCardTransactionsRequest build() {
            CreditCardTransactionsRequest creditCardTransactionsRequest =
                    new CreditCardTransactionsRequest();
            creditCardTransactionsRequest.cards = cards;
            creditCardTransactionsRequest.customerId = customerId;
            creditCardTransactionsRequest.searchFilters = searchFilters;
            creditCardTransactionsRequest.showContractTransactions = showContractTransactions;
            creditCardTransactionsRequest.sortedBy = sortedBy;
            creditCardTransactionsRequest.sortedType = sortedType;
            return creditCardTransactionsRequest;
        }
    }
}
