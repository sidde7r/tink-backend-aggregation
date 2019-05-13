package se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.fetcher.transactionalaccount.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.NordeaSEConstants;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.fetcher.transactionalaccount.entities.TransactionEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class FetchAccountTransactionResponse implements PaginatorResponse {
    @JsonProperty("result")
    private List<TransactionEntity> transactions;

    @Override
    public Collection<? extends Transaction> getTinkTransactions() {
        return toTinkTransactions();
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        return Optional.of(transactions.size() == NordeaSEConstants.Fetcher.CAN_FETCH_MORE);
    }

    public List<TransactionEntity> getTransactions() {
        return transactions;
    }

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    public void setTransactions(List<TransactionEntity> transactions) {
        this.transactions = transactions;
    }

    private Collection<Transaction> toTinkTransactions() {
        return getTransactions().stream()
                .map(TransactionEntity::toTinkTransaction)
                .collect(Collectors.toList());
    }
}
