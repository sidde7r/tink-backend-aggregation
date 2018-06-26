package se.tink.backend.aggregation.agents.nxgen.be.banks.bnppf.entities;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionData {
    private List<Transaction> transaction;

    public List<se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction> getTinkTransactions(String externalAccountId) {
        return Optional.ofNullable(transaction).orElse(Collections.emptyList()).stream()
                .map(transaction -> transaction.toTinkTransaction(externalAccountId))
                .collect(Collectors.toList());
    }
}
