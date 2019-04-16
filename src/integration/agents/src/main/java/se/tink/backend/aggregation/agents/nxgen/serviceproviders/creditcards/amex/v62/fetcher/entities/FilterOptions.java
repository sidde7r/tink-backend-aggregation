package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FilterOptions {

    @JsonProperty("transactionTypes")
    private TransactionTypes transactionTypes;

    @JsonProperty("sort")
    private Sort sort;

    @JsonProperty("cardmembers")
    private Cardmembers cardmembers;

    @JsonProperty("title")
    private String title;

    public TransactionTypes getTransactionTypes() {
        return transactionTypes;
    }

    public Sort getSort() {
        return sort;
    }

    public Cardmembers getCardmembers() {
        return cardmembers;
    }

    public String getTitle() {
        return title;
    }
}
