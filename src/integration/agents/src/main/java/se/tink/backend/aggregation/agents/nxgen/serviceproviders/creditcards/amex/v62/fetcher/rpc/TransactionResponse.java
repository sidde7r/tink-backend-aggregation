package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.assertj.core.util.Lists;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.AmericanExpressV62Configuration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.AmericanExpressV62Predicates;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.entities.ActivityListEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.entities.TransactionDetailsEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class TransactionResponse {
    private TransactionDetailsEntity transactionDetails;
    @JsonIgnore private AmericanExpressV62Configuration config;

    public TransactionDetailsEntity getTransactionDetails() {
        return this.transactionDetails;
    }

    @JsonIgnore
    public PaginatorResponse getPaginatorResponse(AmericanExpressV62Configuration config) {
        return getPaginatorResponse(config, Collections.emptyList());
    }

    @JsonIgnore
    public PaginatorResponse getPaginatorResponse(
            AmericanExpressV62Configuration config, List<Transaction> pendingTransactions) {
        this.config = config;

        List<Transaction> transactions = new ArrayList<>();
        transactions.addAll(pendingTransactions);
        transactions.addAll(parseResponse());

        return PaginatorResponseImpl.create(transactions, hasMoreTransactions());
    }

    // If this page contains error we can't fetch more pages.
    // If it's a proper page we can try to fetch one more
    // Solution used as we don't have information about number of pages
    private List<Transaction> parseResponse() {
        List<Transaction> list = new ArrayList<>();

        if (!hasTransactions()) {
            return Lists.emptyList();
        }

        transactionDetails
                .getActivityList()
                .forEach(
                        activity ->
                                AmericanExpressV62Predicates.getTransactionsFromGivenPage
                                        .apply(activity)
                                        .forEach(
                                                AmericanExpressV62Predicates
                                                        .transformIntoTinkTransactions(
                                                                config, list)));

        return list;
    }

    @JsonIgnore
    private boolean hasTransactions() {
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
}
