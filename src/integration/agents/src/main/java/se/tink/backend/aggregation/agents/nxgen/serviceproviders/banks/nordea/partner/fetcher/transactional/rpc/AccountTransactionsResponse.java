package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.fetcher.transactional.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.google.common.base.Strings;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.fetcher.transactional.entity.TransactionEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class AccountTransactionsResponse {

    @Getter private String continuationKey;
    private List<TransactionEntity> result;
    private int totalSize;

    @JsonIgnore
    public Collection<Transaction> getTinkTransactions() {
        return Optional.ofNullable(result).orElse(Collections.emptyList()).stream()
                .map(TransactionEntity::toTinkTransaction)
                .collect(Collectors.toList());
    }

    public Optional<Boolean> canFetchMore(LocalDate dateLimit) {
        // don't fetch further back than dateLimit
        final Optional<LocalDate> oldestTransactionDate =
                result.stream().map(TransactionEntity::getDate).min(LocalDate::compareTo);
        if (oldestTransactionDate.isPresent() && oldestTransactionDate.get().isBefore(dateLimit)) {
            return Optional.of(false);
        }
        return Optional.of(!Strings.isNullOrEmpty(continuationKey));
    }
}
