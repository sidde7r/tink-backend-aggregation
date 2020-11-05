package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.fetcher.rpc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class TransactionResponse extends ArrayList<TransactionEntity>
        implements TransactionKeyPaginatorResponse<String> {
    private String previousTransactionId = "";

    @Override
    public Collection<? extends Transaction> getTinkTransactions() {
        return this.stream().map(TransactionEntity::toTinkTransaction).collect(Collectors.toList());
    }

    public void setPreviousTransactionId(String previousTransactionId) {
        this.previousTransactionId = previousTransactionId;
    }

    private String getLastTransactionId() {
        if (this.isEmpty()) {
            return null;
        }
        return this.get(this.size() - 1).getId();
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        final String lastTransactionId = getLastTransactionId();
        if (lastTransactionId == null) {
            return Optional.of(false);
        }
        return Optional.of(!lastTransactionId.equals(previousTransactionId));
    }

    @Override
    public String nextKey() {
        return getLastTransactionId();
    }
}
