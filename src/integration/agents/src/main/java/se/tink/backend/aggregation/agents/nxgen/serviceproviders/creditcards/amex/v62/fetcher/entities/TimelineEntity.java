package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.entities;

import java.util.List;
import java.util.Map;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TimelineEntity {
    private int status;
    private int sortedIndex;
    private String message;
    private String messageType;
    private String statusCode;
    private List<SubcardEntity> cardList;
    private List<TimelineItemsEntity> timelineItems;
    private Map<String, TransactionEntity> transactionMap;

    public List<SubcardEntity> getCardList() {
        return cardList;
    }

    public int getStatus() {
        return status;
    }

    public int getSortedIndex() {
        return sortedIndex;
    }

    public String getMessage() {
        return message;
    }

    public String getMessageType() {
        return messageType;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public List<TimelineItemsEntity> getTimelineItems() {
        return timelineItems;
    }

    public Map<String, TransactionEntity> getTransactionMap() {
        return transactionMap;
    }
}
