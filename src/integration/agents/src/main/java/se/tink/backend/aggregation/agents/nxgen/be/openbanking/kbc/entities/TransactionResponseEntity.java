package se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class TransactionResponseEntity implements PaginatorResponse {

    @JsonProperty("transactions")
    private TransactionsEntity transactionsEntity;

    @JsonProperty("account")
    private AccountEntity accountEntity;

    public TransactionsEntity getTransactionsEntity() {
        return transactionsEntity;
    }

    public AccountEntity getAccountEntity() {
        return accountEntity;
    }

    @Override
    public Collection<? extends Transaction> getTinkTransactions() {
        return transactionsEntity.getBooked().stream()
                .map(BookedItemEntity::toTinkTransactions)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        return Optional.empty();
    }
}
