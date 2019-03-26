package se.tink.backend.aggregation.agents.nxgen.de.banks.santander.fetcher.transactional.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class TransactionResultEntity {
    @JsonProperty("bookedTransactionList")
    private List<TransactionEntity> transactions;

    public Collection<Transaction> toTinkTransactions() {
        return transactions.stream()
                .filter(transactionEntity -> transactionEntity.isValid())
                .map(TransactionEntity::toTinkTransaction)
                .collect(Collectors.toList());
    }
}
