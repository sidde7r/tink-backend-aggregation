package se.tink.backend.aggregation.agents.creditcards.americanexpress.v3.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TimelineEntity extends StatusEntity {
    private int sortedIndex;
    private List<TimelineItemGroupEntity> timelineItems;
    private Map<String, TransactionEntity> transactionMap;
    private List<CardEntity> cardList;

    public List<TimelineItemGroupEntity> getTimelineItems() {
        return timelineItems;
    }

    public void setTimelineItems(List<TimelineItemGroupEntity> timelineItems) {
        this.timelineItems = timelineItems;
    }

    public int getSortedIndex() {
        return sortedIndex;
    }

    public void setSortedIndex(int sortedIndex) {
        this.sortedIndex = sortedIndex;
    }

    public Map<String, TransactionEntity> getTransactionMap() {
        return transactionMap;
    }

    public void setTransactionMap(Map<String, TransactionEntity> transactionMap) {
        this.transactionMap = transactionMap;
    }

    public void setCardList(List<CardEntity> cardList) {
        this.cardList = cardList;
    }

    public List<CardEntity> getCardList() {
        return cardList;
    }
}
