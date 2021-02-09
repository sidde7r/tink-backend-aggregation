package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.fetcher.creditcard.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.fetcher.creditcard.entity.CardTransaction;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class CardTransactionListResponse {
    // The number of transactions in the current result
    @JsonProperty private int size;

    // Which page the result represent
    @JsonProperty private int page;

    // Maximum number of transactions on a page
    @JsonProperty private int pageSize;

    @JsonProperty private List<CardTransaction> transactions;

    @JsonIgnore
    public Collection<Transaction> getTinkTransactions() {
        return transactions.stream()
                .map(CardTransaction::toTinkTransaction)
                .collect(Collectors.toList());
    }

    public Optional<Boolean> canFetchMore(LocalDate dateLimit) {
        // don't fetch further back than dateLimit
        final Optional<LocalDate> oldestTransactionDate =
                transactions.stream().map(CardTransaction::getDate).min(LocalDate::compareTo);
        if (oldestTransactionDate.isPresent() && oldestTransactionDate.get().isBefore(dateLimit)) {
            return Optional.of(false);
        }
        return Optional.of(transactions.size() >= pageSize);
    }
}
