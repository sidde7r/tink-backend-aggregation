package se.tink.backend.aggregation.agents.nxgen.ro.banks.raiffeisen.fetcher.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountLinksEntity {
    @JsonProperty("balances")
    private String balanceUrl;
    @JsonProperty("transactions")
    private String transactionUrl;

    public String getBalanceUrl() {
        return balanceUrl;
    }

    public String getTransactionUrl() {
        return transactionUrl;
    }
}
