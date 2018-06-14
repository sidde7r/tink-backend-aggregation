package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.assertj.core.util.Lists;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.AmericanExpressV62Configuration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.AmericanExpressV62Predicates;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.entities.BillingInfoDetailsEntity;
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
    private int pageNo;
    @JsonIgnore
    private List<Transaction> pendingTransactions;

    public TransactionDetailsEntity getTransactionDetails() {
        return this.transactionDetails;
    }

    @Override
    public Collection<? extends Transaction> getTinkTransactions() {
        return parseResponse();
    }

    @Override
    public boolean canFetchMore() {
        int maxPageNo = transactionDetails.getBillingInfo().getBillingInfoDetails().stream()
                .mapToInt(BillingInfoDetailsEntity::getPageNo).max().orElse(0);
        return maxPageNo > pageNo;
    }

    public TransactionResponse setConfig(AmericanExpressV62Configuration config) {
        this.config = config;
        return this;
    }

    public TransactionPagePaginatorResponse getPaginatorResponse(int pageNo, List<Transaction> pendingTransactions) {
        this.pageNo = pageNo;
        this.pendingTransactions = pendingTransactions;
        return this;
    }

    private List<Transaction> parseResponse() {
        List<Transaction> list = new ArrayList<>(pendingTransactions);

        Optional.ofNullable(this.transactionDetails.getActivityList()).orElseGet(Lists::emptyList)
            .forEach(
                activity ->
                    AmericanExpressV62Predicates.getTransactionsFromGivenPage.apply(activity)
                        .forEach(AmericanExpressV62Predicates
                            .transformIntoTinkTransactions(config, list))
            );
        return list;
    }
}
