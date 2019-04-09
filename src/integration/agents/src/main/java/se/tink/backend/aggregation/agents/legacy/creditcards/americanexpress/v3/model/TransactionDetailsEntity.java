package se.tink.backend.aggregation.agents.creditcards.americanexpress.v3.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionDetailsEntity extends StatusEntity {
    private List<CardEntity> cardList;
    private BillingInfoEntity billingInfo;
    private List<ActivityEntity> activityList;

    public List<CardEntity> getCardList() {
        return cardList;
    }

    public void setCardList(List<CardEntity> cardList) {
        this.cardList = cardList;
    }

    public BillingInfoEntity getBillingInfo() {
        return billingInfo;
    }

    public void setBillingInfo(BillingInfoEntity billingInfo) {
        this.billingInfo = billingInfo;
    }

    public List<ActivityEntity> getActivityList() {
        return activityList;
    }

    public void setActivityList(List<ActivityEntity> activityList) {
        this.activityList = activityList;
    }
}
