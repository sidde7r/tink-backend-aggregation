package se.tink.backend.aggregation.agents.banks.se.icabanken.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionListBody {
    @JsonProperty("Transactions")
    private List<TransactionEntity> transactions;
    @JsonProperty("NoMoreTransactions")
    private boolean noMoreTransactions;
    @JsonProperty("FromDate")
    private String fromDate;

    public String getFromDate() {
        return fromDate;
    }

    public void setFromDate(String fromDate) {
        this.fromDate = fromDate;
    }

    public String getToDate() {
        return toDate;
    }

    public void setToDate(String toDate) {
        this.toDate = toDate;
    }

    @JsonProperty("ToDate")
    private String toDate;

    public List<TransactionEntity> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<TransactionEntity> transactions) {
        this.transactions = transactions;
    }

    public boolean isNoMoreTransactions() {
        return noMoreTransactions;
    }

    public void setNoMoreTransactions(boolean noMoreTransactions) {
        this.noMoreTransactions = noMoreTransactions;
    }

}
