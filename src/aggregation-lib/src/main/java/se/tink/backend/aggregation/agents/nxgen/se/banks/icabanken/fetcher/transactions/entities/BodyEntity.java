package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.transactions.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BodyEntity {
    @JsonProperty("Transactions")
    private List<TransactionsEntity> transactions;
    @JsonProperty("NoMoreTransactions")
    private boolean noMoreTransactions;
    @JsonProperty("FromDate")
    private String fromDate;
    @JsonProperty("ToDate")
    private String toDate;

    public List<TransactionsEntity> getTransactions() {
        return transactions;
    }

    public boolean isNoMoreTransactions() {
        return noMoreTransactions;
    }

    public String getFromDate() {
        return fromDate;
    }

    public String getToDate() {
        return toDate;
    }
}
