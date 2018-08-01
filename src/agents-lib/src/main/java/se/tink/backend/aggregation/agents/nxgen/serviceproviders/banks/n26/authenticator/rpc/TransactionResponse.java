package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.authenticator.rpc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.authenticator.entities.TransactionEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class TransactionResponse extends ArrayList<TransactionEntity> implements
        TransactionKeyPaginatorResponse<String> {

    @Override
    public Collection<? extends Transaction> getTinkTransactions() {
        return this.stream()
                .map(TransactionEntity::toTinkTransaction)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        return Optional.of(false);
    }

    @Override
    public String nextKey() {
        return null;
    }
}
