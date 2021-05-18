package se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.fetcher.transactionalaccount.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.assertj.core.util.Strings;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.fetcher.transactionalaccount.entities.TransactionEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class FetchTransactionResponse implements TransactionKeyPaginatorResponse<String> {
    @JsonProperty("result")
    private List<TransactionEntity> transactions;

    @JsonProperty("continuation_key")
    private String continuationKey;

    @Override
    public Collection<? extends Transaction> getTinkTransactions() {
        return transactions.stream()
                .map(TransactionEntity::toTinkTransaction)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        final boolean hasContinuationKey = !Strings.isNullOrEmpty(continuationKey);
        return Optional.of(hasContinuationKey);
    }

    @Override
    public String nextKey() {
        return continuationKey;
    }
}
