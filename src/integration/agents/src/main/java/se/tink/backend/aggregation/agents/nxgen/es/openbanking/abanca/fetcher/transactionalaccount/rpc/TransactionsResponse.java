package se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.fetcher.transactionalaccount.rpc;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.fetcher.transactionalaccount.entity.transaction.TransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.fetcher.transactionalaccount.entity.transaction.TransactionLinksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class TransactionsResponse implements TransactionKeyPaginatorResponse<String> {

    private List<TransactionEntity> data;
    private TransactionLinksEntity links;

    @Override
    public Collection<? extends Transaction> getTinkTransactions() {
        return data.stream().map(TransactionEntity::toTinkTransaction).collect(Collectors.toList());
    }

    @Override
    public Optional<Boolean> canFetchMore() {

        return Optional.of(
                Optional.ofNullable(links).map(TransactionLinksEntity::hasNext).orElse(false));
    }

    @Override
    public String nextKey() {
        return links.getNext();
    }
}
