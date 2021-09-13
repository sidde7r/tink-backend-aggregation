package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.fetcher.transactionalaccount.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.fetcher.transactionalaccount.entity.common.PaginationEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.fetcher.transactionalaccount.entity.transaction.TransactionEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class TransactionsResponse implements TransactionKeyPaginatorResponse<String> {

    @JsonProperty("_links")
    private PaginationEntity links;

    private List<TransactionEntity> transactions;

    @Override
    public String nextKey() {
        return Optional.ofNullable(links).map(PaginationEntity::getNext).orElse(null);
    }

    @Override
    public List<Transaction> getTinkTransactions() {
        return getTransactions().stream()
                .map(TransactionEntity::toTinkTransaction)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        return Optional.of(links != null && links.hasNext());
    }

    private List<TransactionEntity> getTransactions() {
        return Optional.ofNullable(transactions).orElseGet(Collections::emptyList);
    }
}
