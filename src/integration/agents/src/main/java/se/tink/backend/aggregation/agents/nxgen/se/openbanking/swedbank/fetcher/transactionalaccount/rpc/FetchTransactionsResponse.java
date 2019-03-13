package se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.fetcher.transactionalaccount.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.fetcher.transactionalaccount.entity.transaction.TransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.fetcher.transactionalaccount.entity.transaction.TransactionsEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class FetchTransactionsResponse implements PaginatorResponse {

    private TransactionsEntity transactions;

    @Override
    public Optional<Boolean> canFetchMore() {
        return Optional.of(false);
    }

    @JsonIgnore
    private Transaction toTinkTransaction(
            final TransactionEntity transaction, final boolean isPending) {
        return Transaction.builder()
                .setAmount(transaction.getAmount())
                .setDate(transaction.getValueDate())
                .setDescription(transaction.getRemittanceInformationUnstructured())
                .setPending(isPending)
                .build();
    }

    @JsonIgnore
    @Override
    public Collection<? extends Transaction> getTinkTransactions() {
        return Stream.concat(
                        transactions.getBooked().orElse(Collections.emptyList()).stream()
                                .map(t -> toTinkTransaction(t, false)),
                        transactions.getPending().orElse(Collections.emptyList()).stream()
                                .map(t -> toTinkTransaction(t, true)))
                .collect(Collectors.toList());
    }
}
