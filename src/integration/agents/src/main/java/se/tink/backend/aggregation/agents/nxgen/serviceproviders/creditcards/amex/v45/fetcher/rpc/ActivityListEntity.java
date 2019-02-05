package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v45.fetcher.rpc;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ActivityListEntity {
    private List<TransactionEntity> transactionList;
    private String billingIndex;
    private List<TransactionTypesEntity> transactionTypes;
    private boolean flexEnrolled;

    public List<TransactionEntity> getTransactionList() {
        return transactionList;
    }

    public String getBillingIndex() {
        return billingIndex;
    }

    public List<TransactionTypesEntity> getTransactionTypes() {
        return transactionTypes;
    }

    public boolean isFlexEnrolled() {
        return flexEnrolled;
    }
}
