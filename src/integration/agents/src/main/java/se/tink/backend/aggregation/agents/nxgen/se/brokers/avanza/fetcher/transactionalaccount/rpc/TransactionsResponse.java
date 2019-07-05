package se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.transactionalaccount.rpc;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.transactionalaccount.entities.TransactionEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class TransactionsResponse {
    private int totalNumberOfTransactions;
    private List<TransactionEntity> transactions;

    public int getTotalNumberOfTransactions() {
        return totalNumberOfTransactions;
    }

    public List<Transaction> getTransactions() {
        return Optional.ofNullable(transactions).orElseGet(Collections::emptyList).stream()
                .filter(TransactionEntity::isDepositOrWithdraw)
                .map(TransactionEntity::toTinkTransaction)
                .collect(Collectors.toList());
    }
}
