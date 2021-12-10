package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.fetcher.rpc;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.fetcher.entity.AccountInfoEntity;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.fetcher.entity.TransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.fetcher.entity.TransactionsEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
@SuppressWarnings("UnusedDeclaration")
public class TransactionsResponse implements PaginatorResponse {

    private AccountInfoEntity account;
    private TransactionsEntity transactions;

    @Override
    public Collection<? extends Transaction> getTinkTransactions() {
        return transactions.getBooked().stream()
                .map(TransactionEntity::toTinkBookedTransaction)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        return Optional.empty();
    }
}
