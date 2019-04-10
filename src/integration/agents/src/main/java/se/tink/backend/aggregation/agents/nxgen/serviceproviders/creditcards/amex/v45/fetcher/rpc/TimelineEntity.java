package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v45.fetcher.rpc;

import java.util.List;
import java.util.Map;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TimelineEntity extends ResponseStatusEntity {
    private int sortedIndex;
    private List<TimelineItemsEntity> timelineItems;
    private Map<String, TransactionEntity> transactionMap;

    public int getSortedIndex() {
        return sortedIndex;
    }

    public List<TimelineItemsEntity> getTimelineItems() {
        return timelineItems;
    }

    public Map<String, TransactionEntity> getTransactionMap() {
        return transactionMap;
    }
}
