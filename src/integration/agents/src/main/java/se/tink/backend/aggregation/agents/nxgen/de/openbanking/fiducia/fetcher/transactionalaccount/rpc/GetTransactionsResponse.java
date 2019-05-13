package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.fetcher.transactionalaccount.rpc;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.fetcher.transactionalaccount.entities.BalanceEntity;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.fetcher.transactionalaccount.entities.TransactionsEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class GetTransactionsResponse implements PaginatorResponse {

    private AccountEntity account;
    private List<BalanceEntity> balances;
    private TransactionsEntity transactions;

    @Override
    public Collection<? extends Transaction> getTinkTransactions() {
        return Optional.ofNullable(transactions)
                .orElse(new TransactionsEntity())
                .toTinkTransactions();
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        return Optional.empty();
    }
}
