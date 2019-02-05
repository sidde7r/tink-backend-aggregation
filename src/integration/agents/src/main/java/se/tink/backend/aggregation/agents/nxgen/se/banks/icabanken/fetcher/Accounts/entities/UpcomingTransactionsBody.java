package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.accounts.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class UpcomingTransactionsBody {
    @JsonProperty("Assignments")
    private List<UpcomingTransactionEntity> upcomingTransactions;
    @JsonProperty("CurrentMonthTotalAmount")
    private double currentMonthTotalAmount;
    @JsonProperty("TotalAmount")
    private double totalAmount;

    public List<UpcomingTransactionEntity> getUpcomingTransactions() {
        return upcomingTransactions;
    }

    public double getCurrentMonthTotalAmount() {
        return currentMonthTotalAmount;
    }

    public double getTotalAmount() {
        return totalAmount;
    }
}
