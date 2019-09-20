package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.fetcher.rpc;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.fetcher.entities.TransactionsEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class TransactionsResponse implements PaginatorResponse {

    private final List<FetchTransactionsResponse> responseList;

    public TransactionsResponse(List<FetchTransactionsResponse> responseList) {
        this.responseList = responseList;
    }

    @Override
    public Collection<? extends Transaction> getTinkTransactions() {
        return Optional.ofNullable(responseList)
                .map(List::stream)
                .orElse(Stream.empty())
                .map(FetchTransactionsResponse::getTransactions)
                .filter(Objects::nonNull)
                .flatMap(TransactionsEntity::toTinkTransactions)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        return Optional.empty();
    }
}
