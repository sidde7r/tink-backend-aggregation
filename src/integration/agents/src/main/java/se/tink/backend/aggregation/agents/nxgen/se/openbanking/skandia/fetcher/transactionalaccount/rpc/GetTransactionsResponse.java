package se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.fetcher.transactionalaccount.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.fetcher.transactionalaccount.entities.TransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.fetcher.transactionalaccount.entities.TransactionsEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
@Getter
public class GetTransactionsResponse implements TransactionKeyPaginatorResponse<String> {
    private AccountEntity account;
    private TransactionsEntity transactions;

    @Override
    @JsonIgnore
    public Collection<? extends Transaction> getTinkTransactions() {
        return transactions.toTinkTransactions();
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        return Optional.of(transactions.getLinks().hasMore());
    }

    public String nextKey() {
        return transactions.getLinks().getNext();
    }

    @JsonIgnore
    public void setPending(List<TransactionEntity> pending) {
        transactions.setPending(pending);
    }

    @JsonIgnore
    public List<TransactionEntity> getPending() {
        return transactions.getPending();
    }
}
