package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.entities;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionDetailsEntity {
    private int status;
    private String messageType;
    private BillingInfoEntity billingInfo;
    private List<ActivityListEntity> activityList;
    private FilterOptions filterOptions;

    public int getStatus() {
        return status;
    }

    public String getMessageType() {
        return messageType;
    }

    public BillingInfoEntity getBillingInfo() {
        return billingInfo;
    }

    public List<ActivityListEntity> getActivityList() {
        return activityList;
    }

    public FilterOptions getFilterOptions() {
        return filterOptions;
    }
}
