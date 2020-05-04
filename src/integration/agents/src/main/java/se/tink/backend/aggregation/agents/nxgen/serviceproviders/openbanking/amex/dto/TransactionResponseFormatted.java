package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.dto;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class TransactionResponseFormatted implements PaginatorResponse {

    private Integer totalTransactionCount;

    private List<TransactionDto> transactions;

    public TransactionResponseFormatted(
            Integer totalTransactionCount, List<TransactionDto> transactions) {
        this.totalTransactionCount = totalTransactionCount;
        this.transactions = transactions;
    }

    @Override
    public Collection<? extends Transaction> getTinkTransactions() {
        return transactions.stream()
                .map(TransactionDto::toTinkTransaction)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        return Optional.empty();
    }
}
