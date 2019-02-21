package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.creditcard.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.creditcard.entities.CreditCardTransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.creditcard.entities.CreditCardTransactionsPaginationEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.utils.BbvaUtils;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.CreditCardTransaction;

@JsonObject
public class CreditCardTransactionsResponse implements TransactionKeyPaginatorResponse<String> {

    private boolean moreResults;
    private String totalResults;
    private CreditCardTransactionsPaginationEntity pagination;
    private List<CreditCardTransactionEntity> items;

    @JsonIgnore
    @Override
    public Collection<CreditCardTransaction> getTinkTransactions() {
        return items.stream().map(t -> t.toTinkTransaction()).collect(Collectors.toList());
    }

    @JsonIgnore
    @Override
    public Optional<Boolean> canFetchMore() {
        return Optional.ofNullable(moreResults);
    }

    @JsonIgnore
    @Override
    public String nextKey() {
        if (moreResults) {
            String nextPageLink = this.pagination.getNextPage();
            return BbvaUtils.splitUtlGetKey(nextPageLink).orElseGet(() -> "");
        }
        throw new IllegalStateException("Trying to paginate when no more pages.");
    }
}
