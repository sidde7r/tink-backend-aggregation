package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.fetcher.transactionalaccount.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.fetcher.transactionalaccount.entities.BalanceEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.fetcher.transactionalaccount.entities.TransactionsEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class GetTransactionsBalancesResponse implements PaginatorResponse {
    private TransactionsEntity transactions;
    private List<BalanceEntity> balances;

    @JsonIgnore private boolean pageRemaining;

    @Override
    public Collection<? extends Transaction> getTinkTransactions() {
        return Optional.ofNullable(transactions)
                .orElse(new TransactionsEntity())
                .getTinkTransactions();
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        // temporary pagination disabled
        return Optional.of(false);
    }

    public TransactionsEntity getTransactions() {
        return transactions;
    }

    public List<BalanceEntity> getBalances() {
        return balances;
    }

    public GetTransactionsBalancesResponse setPageRemaining(boolean pageRemaining) {
        this.pageRemaining = pageRemaining;
        return this;
    }

    public void setBalances(List<BalanceEntity> balances) {
        this.balances = balances;
    }
}
