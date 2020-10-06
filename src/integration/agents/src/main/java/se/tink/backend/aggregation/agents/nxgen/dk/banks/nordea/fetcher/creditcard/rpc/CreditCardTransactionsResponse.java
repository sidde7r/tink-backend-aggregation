package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.creditcard.rpc;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.creditcard.entities.CreditCardTransactionEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@Getter
@JsonObject
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class CreditCardTransactionsResponse implements TransactionKeyPaginatorResponse<Integer> {
    private int page;
    private int pageSize;
    private int size;

    private List<CreditCardTransactionEntity> transactions;

    @Override
    public Integer nextKey() {
        return page + 1;
    }

    @Override
    public Collection<? extends Transaction> getTinkTransactions() {
        return transactions.stream()
                .map(CreditCardTransactionEntity::toTinkTransaction)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        boolean returnedPageIsFull = size == pageSize;
        return Optional.of(returnedPageIsFull);
    }
}
