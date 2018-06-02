package se.tink.backend.aggregation.agents.banks.lansforsakringar.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UpcomingTransactionListResponse {
    private List<UpcomingTransactionEntity> upcomingTransactions;

    public List<UpcomingTransactionEntity> getUpcomingTransactions() {
        return upcomingTransactions;
    }

    public void setUpcomingTransactions(List<UpcomingTransactionEntity> upcomingTransactions) {
        this.upcomingTransactions = upcomingTransactions;
    }
}
