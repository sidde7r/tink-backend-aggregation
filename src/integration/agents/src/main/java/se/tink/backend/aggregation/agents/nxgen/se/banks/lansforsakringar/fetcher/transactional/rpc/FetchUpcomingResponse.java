package se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.transactional.rpc;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.transactional.entity.CurrentMonthEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.transactional.entity.ErrorMessagesEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.transactional.entity.UpcomingTransactionsEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FetchUpcomingResponse {
    private List<UpcomingTransactionsEntity> upcomingTransactions;
    private List<ErrorMessagesEntity> errorMessages;
    private double sumOfUpcomingTransactions;
    private CurrentMonthEntity currentMonth;

    public List<UpcomingTransactionsEntity> getUpcomingTransactions() {
        return upcomingTransactions;
    }
}
