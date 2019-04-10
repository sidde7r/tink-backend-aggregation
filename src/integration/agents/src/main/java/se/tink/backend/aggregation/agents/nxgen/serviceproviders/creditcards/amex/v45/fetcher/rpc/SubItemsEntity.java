package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v45.fetcher.rpc;

import java.util.Map;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v45.AmericanExpressConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SubItemsEntity {
    private String type;
    private String id;
    private String date;
    private String title;

    public String getType() {
        return type;
    }

    public String getId() {
        return id;
    }

    public String getDate() {
        return date;
    }

    public String getTitle() {
        return title;
    }

    public boolean isPending() {
        return type.equals(AmericanExpressConstants.Tags.IS_PENDING);
    }

    public boolean belongToAccount(Integer cardIndex, TimelineEntity timeline) {
        Map<String, TransactionEntity> transactionEntityMap = timeline.getTransactionMap();
        /*
           Amex timeline items shows the recent transactions and other account activities
           such as bonus points
           e.g. "Få upp till 18 000 Extrapoäng per godkänd ansökan!"
           id of these timeline items contains strings.
           this differs from the normal transaction that contains numeric id.
        */
        if (!transactionEntityMap.containsKey(this.id)) {
            return false;
        }
        Integer suppIndex = Integer.valueOf(transactionEntityMap.get(this.id).getSuppIndex());
        return suppIndex.equals(cardIndex);
    }
}
