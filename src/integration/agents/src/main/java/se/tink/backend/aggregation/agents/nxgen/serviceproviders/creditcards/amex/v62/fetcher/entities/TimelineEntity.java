package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.AmericanExpressV62Configuration;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

@JsonObject
public class TimelineEntity {
    private int status;
    private int sortedIndex;
    private String message;
    private String messageType;
    private String statusCode;
    private List<SubcardEntity> cardList = new ArrayList<>();
    private List<TimelineItemsEntity> timelineItems;
    private Map<String, TransactionEntity> transactionMap;

    public List<SubcardEntity> getCardList() {
        return cardList;
    }

    public List<TimelineItemsEntity> getTimelineItems() {
        return timelineItems;
    }

    public Map<String, TransactionEntity> getTransactionMap() {
        return transactionMap;
    }

    @JsonIgnore
    public List<CreditCardAccount> getCreditCardAccounts(
            final AmericanExpressV62Configuration configuration) {
        return cardList.stream()
                .map(subcardEntity -> subcardEntity.toCreditCardAccount(configuration))
                .collect(Collectors.toList());
    }

    @JsonIgnore
    public Set<TransactionEntity> getTransactions(final String suppIndex) {
        Map<String, Boolean> transactionPendingMap = new HashMap<>();

        // Fetch all pending transaction ids from timeline sub items.
        timelineItems.stream()
                .flatMap(timeLineItem -> timeLineItem.getSubItems().stream())
                .filter(SubItemsEntity::isPending)
                .forEach(
                        subItem -> transactionPendingMap.put(subItem.getId(), subItem.isPending()));

        // Map pending transaction ids to the transaction map to get transaction details.
        return getTransactionMap().entrySet().stream()
                .filter(entry -> suppIndex.equalsIgnoreCase(entry.getValue().getSuppIndex()))
                .filter(entry -> transactionPendingMap.containsKey(entry.getKey()))
                .map(entry -> entry.getValue().setPending(true))
                .collect(Collectors.toSet());
    }
}
