package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v45.fetcher.rpc;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionDetailsEntity extends ResponseStatusEntity {

    private BillingInfoEntity billingInfo;
    private List<ActivityListEntity> activityList;
    private List<ShortCardEntitySummary> cardList;

    public List<ShortCardEntitySummary> getCardList() {
        return cardList;
    }

    public BillingInfoEntity getBillingInfo() {
        return billingInfo;
    }

    public List<ActivityListEntity> getActivityList() {
        return activityList;
    }
}
