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
    private final String providerMarket;

    public TransactionResponseFormatted(
            List<TransactionDto> transactions, LocalDate nextEndDate, String providerMarket) {
        this.transactions = transactions;
        this.nextEndDate = nextEndDate;
        this.providerMarket = providerMarket;
    }

    @Override
    public String nextKey() {
        if (nextEndDate == null) {
            return null;
        }
        return nextEndDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    @Override
    public Collection<? extends Transaction> getTinkTransactions() {
        return transactions.stream()
                .map(transactionDto -> transactionDto.toTinkTransaction(providerMarket))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        return Optional.of(nextEndDate != null);
    }
}
