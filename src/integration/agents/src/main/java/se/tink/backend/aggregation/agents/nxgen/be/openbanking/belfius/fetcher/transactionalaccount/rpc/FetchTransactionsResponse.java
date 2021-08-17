package se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.fetcher.transactionalaccount.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.fetcher.transactionalaccount.entity.transaction.EmbeddedEntity;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.fetcher.transactionalaccount.entity.transaction.TransactionEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@Slf4j
@JsonObject
public class FetchTransactionsResponse implements TransactionKeyPaginatorResponse<String> {

    @JsonProperty("_embedded")
    private EmbeddedEntity embedded;

    @Override
    public String nextKey() {
        return embedded.getNextPageKey();
    }

    @Override
    public Collection<? extends Transaction> getTinkTransactions() {
        return embedded.getTransactions().stream()
                .map(TransactionEntity::toTinkTransaction)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        log.info("Transactions amount on page is : {}", embedded.getTransactions().size());
        return Optional.of(
                embedded.getNextPageKey() != null && !embedded.getTransactions().isEmpty());
    }
}
