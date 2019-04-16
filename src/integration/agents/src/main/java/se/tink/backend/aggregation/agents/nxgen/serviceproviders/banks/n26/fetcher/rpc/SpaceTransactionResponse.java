package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.fetcher.rpc;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.codehaus.jackson.annotate.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class SpaceTransactionResponse implements TransactionKeyPaginatorResponse<String> {

    @JsonProperty("transactions")
    public List<SpaceTransactionEntitiy> transactions;

    public boolean hasMore;

    @Override
    public String nextKey() {
        if (transactions.isEmpty()) {
            return null;
        }
        return transactions.get(transactions.size() - 1).getId();
    }

    @Override
    public Collection<? extends Transaction> getTinkTransactions() {
        return transactions.stream()
                .map(SpaceTransactionEntitiy::toTinkTransaction)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        return Optional.of(hasMore);
    }
}
