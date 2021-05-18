package se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.fetcher.creditcard.rpc;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.fetcher.creditcard.entities.CardTransactionEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class FetchCardTransactionsResponse implements PaginatorResponse {
    private List<CardTransactionEntity> transactions;
    private int size;

    @Override
    public Collection<? extends Transaction> getTinkTransactions() {
        return transactions != null
                ? transactions.stream()
                        .map(CardTransactionEntity::toTinkCreditCardTransaction)
                        .collect(Collectors.toList())
                : Collections.emptyList();
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        return Optional.of(isMoreTransactions());
    }

    private boolean isMoreTransactions() {
        return size != 0;
    }
}
