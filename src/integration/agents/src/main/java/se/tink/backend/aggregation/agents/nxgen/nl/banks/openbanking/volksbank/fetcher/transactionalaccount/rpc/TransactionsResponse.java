package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.fetcher.transactionalaccount.rpc;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.entities.transactions.TransactionsEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class TransactionsResponse implements PaginatorResponse {

    private final List<TransactionResponse> responseList;

    public TransactionsResponse(List<TransactionResponse> responseList) {
        this.responseList = responseList;
    }

    @Override
    public Collection<? extends Transaction> getTinkTransactions() {
        return Optional.ofNullable(responseList).orElse(Collections.emptyList()).stream()
                .map(TransactionResponse::getTransactions)
                .filter(Objects::nonNull)
                .map(TransactionsEntity::toTinkTransactions)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        return Optional.empty();
    }
}
