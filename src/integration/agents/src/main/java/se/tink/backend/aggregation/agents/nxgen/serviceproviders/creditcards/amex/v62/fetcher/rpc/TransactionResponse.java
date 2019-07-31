package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.rpc;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.AmericanExpressV62Constants.Fetcher.DEFAULT_MAX_BILLING_INDEX;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.entities.ActivityListEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.entities.BillingInfoDetailsEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.entities.TransactionDetailsEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.entities.TransactionEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionResponse {
    private TransactionDetailsEntity transactionDetails;

    public TransactionDetailsEntity getTransactionDetails() {
        return this.transactionDetails;
    }

    @JsonIgnore
    public int getHighestBillingIndex() {
        if (!isValidResponse()) {
            return DEFAULT_MAX_BILLING_INDEX;
        }
        return transactionDetails.getBillingInfo().getBillingInfoDetails().stream()
                .map(BillingInfoDetailsEntity::getPageNo)
                .mapToInt(v -> v)
                .max()
                .orElse(DEFAULT_MAX_BILLING_INDEX);
    }

    @JsonIgnore
    public boolean isValidResponse() {
        return transactionDetails.getStatus() == 0 && hasTransactions();
    }

    @JsonIgnore
    public boolean hasTransactions() {
        int numTransctions = 0;
        if (hasMoreTransactions()) {
            for (ActivityListEntity activityListEntity : transactionDetails.getActivityList()) {
                if (activityListEntity.getTransactionList() != null) {
                    numTransctions += activityListEntity.getTransactionList().size();
                }
            }
        }

        return numTransctions > 0;
    }

    // if there is no activityList we will not be able to fetch more transactions
    @JsonIgnore
    private boolean hasMoreTransactions() {
        return transactionDetails.getActivityList() != null;
    }

    @JsonIgnore
    public boolean canFetchMore() {
        return hasMoreTransactions() && hasTransactions();
    }

    @JsonIgnore
    public List<TransactionEntity> getTransactionList() {
        return transactionDetails.getActivityList().stream()
                .filter(activityListEntity -> !activityListEntity.getTransactionList().isEmpty())
                .findFirst()
                .map(ActivityListEntity::getTransactionList)
                .orElse(new ArrayList<>());
    }
}
