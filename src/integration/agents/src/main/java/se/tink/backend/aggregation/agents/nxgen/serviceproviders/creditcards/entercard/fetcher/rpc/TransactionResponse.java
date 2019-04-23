package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.entercard.fetcher.rpc;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@SuppressWarnings("unused")
@JsonObject
public class TransactionResponse implements PaginatorResponse {

    private List<TransactionEntity> transactions = null;
    private Pagination pagination;

    public List<TransactionEntity> getTransactions() {
        return transactions;
    }

    public Pagination getPagination() {
        return pagination;
    }

    @Override
    public Collection<? extends Transaction> getTinkTransactions() {
        return transactions.stream()
                .map(TransactionEntity::toTinkTransaction)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        return Optional.of(pagination.canFetchMore());
    }
}
