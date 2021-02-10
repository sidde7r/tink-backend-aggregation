package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.fetcher.transactionalaccount.rpc;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.fetcher.transactionalaccount.entities.TransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.fetcher.transactionalaccount.entities.TransactionsResponseEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.rpc.NordeaBaseResponse;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class GetTransactionsResponse<T extends TransactionEntity> extends NordeaBaseResponse
        implements TransactionKeyPaginatorResponse<String> {

    private TransactionsResponseEntity<T> response;

    @Override
    public Collection<? extends Transaction> getTinkTransactions() {
        return Optional.ofNullable(response)
                .map(TransactionsResponseEntity::toTinkTransactions)
                .orElse(Collections.emptyList());
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        return Optional.of(nextKey() != null);
    }

    @Override
    public String nextKey() {
        return response != null ? response.nextKey() : null;
    }

    protected TransactionsResponseEntity<T> getResponse() {
        return response;
    }
}
