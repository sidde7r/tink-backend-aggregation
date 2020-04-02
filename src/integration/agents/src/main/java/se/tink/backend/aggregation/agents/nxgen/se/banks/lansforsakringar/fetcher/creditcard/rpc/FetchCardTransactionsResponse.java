package se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.creditcard.rpc;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.creditcard.entities.TransactionsEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class FetchCardTransactionsResponse implements PaginatorResponse {

    private List<TransactionsEntity> transactions;
    private boolean morePages;
    private double announcedAmount;

    @Override
    public Collection<? extends Transaction> getTinkTransactions() {
        return transactions == null
                ? Collections.emptyList()
                : transactions.stream()
                        .map(TransactionsEntity::toTinkTransaction)
                        .collect(Collectors.toList());
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        return Optional.of(morePages);
    }
}
