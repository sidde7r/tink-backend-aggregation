package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.dto;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class TransactionResponseFormatted implements TransactionKeyPaginatorResponse<String> {

    private List<TransactionDto> transactions;
    private final LocalDate nextEndDate;

    public TransactionResponseFormatted(List<TransactionDto> transactions, LocalDate nextEndDate) {
        this.transactions = transactions;
        this.nextEndDate = nextEndDate;
    }

    @Override
    public String nextKey() {
        return nextEndDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    @Override
    public Collection<? extends Transaction> getTinkTransactions() {
        return transactions.stream()
                .map(TransactionDto::toTinkTransaction)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        return Optional.of(nextEndDate != null);
    }
}
