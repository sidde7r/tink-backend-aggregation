package se.tink.backend.aggregation.agents.banks.nordea.v18.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionListResponse {
    @JsonProperty("getAccountTransactionsOut")
    private AccountTransactionsEntity accountTransactions;

    @JsonProperty("getCreditCardTransactionsOut")
    private CreditCardTransactionsEntity creditCardTransactions;

    public AccountTransactionsEntity getAccountTransactions() {
        return accountTransactions;
    }

    public CreditCardTransactionsEntity getCreditCardTransactions() {
        return creditCardTransactions;
    }

    public void setAccountTransactions(AccountTransactionsEntity accountTransactions) {
        this.accountTransactions = accountTransactions;
    }

    public void setCreditCardTransactions(CreditCardTransactionsEntity creditCardTransactions) {
        this.creditCardTransactions = creditCardTransactions;
    }
}
