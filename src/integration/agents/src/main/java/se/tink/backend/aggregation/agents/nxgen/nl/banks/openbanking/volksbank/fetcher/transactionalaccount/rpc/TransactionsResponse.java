package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.fetcher.transactionalaccount.rpc;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@RequiredArgsConstructor
public class TransactionsResponse implements PaginatorResponse {

    private final List<TransactionResponse> responseList;
    private final Date limitDate;

    @Override
    public Collection<? extends Transaction> getTinkTransactions() {
        return Optional.ofNullable(responseList).orElse(Collections.emptyList()).stream()
                .map(t -> t.getTransactions(limitDate))
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        return Optional.empty();
    }
}
