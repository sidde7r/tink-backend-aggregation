package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.entities;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ActivityListEntity {
    private List<TransactionEntity> transactionList;
    private String billingIndex;
    //    private boolean flexEnrolled;

    public List<TransactionEntity> getTransactionList() {
        return transactionList;
    }

    public String getBillingIndex() {
        return billingIndex;
    }

    //    public boolean isFlexEnrolled() {
    //        return flexEnrolled;
    //    }
}
