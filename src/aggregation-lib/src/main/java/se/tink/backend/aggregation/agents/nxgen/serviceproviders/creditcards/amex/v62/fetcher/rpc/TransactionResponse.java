package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.assertj.core.util.Lists;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.AmericanExpressV62Configuration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.AmericanExpressV62Predicates;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.entities.TransactionDetailsEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class TransactionResponse implements TransactionPagePaginatorResponse {
    private TransactionDetailsEntity transactionDetails;
    private AmericanExpressV62Configuration config;
    @JsonIgnore
    private boolean canStillFetch = true;
    @JsonIgnore
    private Optional<List<Transaction>> tinkTransactions = Optional.empty();

    public TransactionDetailsEntity getTransactionDetails() {
        return this.transactionDetails;
    }

    @Override
    public Collection<? extends Transaction> getTinkTransactions() {
        List<Transaction> transactions = tinkTransactions.orElseGet(() -> parseResponse());
        return transactions;
    }

    // Hack to know if we can fetch more transaction pages
    // if Tink transaction Optional is empty it means that the response was not parsed, otherwise it was and we need
    // only the status
    @Override
    public boolean canFetchMore() {
        return tinkTransactions.map(t -> canStillFetch).orElseGet(() -> {
            this.parseResponse();
            return canStillFetch;
        });
    }

    public TransactionResponse setConfig(AmericanExpressV62Configuration config) {
        this.config = config;
        return this;
    }

    public TransactionPagePaginatorResponse getPaginatorResponse() {
        return this;
    }

    // If this page contains error we can't fetch more pages.
    // If it's a proper page we can try to fetch one more
    // Solution used as we don't have information about number of pages
    protected List<Transaction> parseResponse() {
        List<Transaction> list = new ArrayList<>();
        Optional.ofNullable(this.transactionDetails.getActivityList()).orElseGet(() -> {
            this.canStillFetch = false;
            return Lists.emptyList();
        })
                .forEach(
                        activity ->
                                AmericanExpressV62Predicates.getTransactionsFromGivenPage.apply(activity)
                                        .forEach(AmericanExpressV62Predicates
                                                .transformIntoTinkTransactions(config, list))
                );
        this.tinkTransactions = Optional.of(list);
        return list;
    }
}
