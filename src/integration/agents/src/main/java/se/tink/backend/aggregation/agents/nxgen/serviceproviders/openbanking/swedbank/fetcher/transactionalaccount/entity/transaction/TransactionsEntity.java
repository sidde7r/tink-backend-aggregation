package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.fetcher.transactionalaccount.entity.transaction;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;

@JsonObject
public class TransactionsEntity {
    private List<TransactionEntity> booked;
    private List<TransactionEntity> pending;

    public List<TransactionEntity> getBooked() {
        return Optional.ofNullable(booked).orElseGet(Lists::newArrayList);
    }

    public List<TransactionEntity> getPending() {
        return Optional.ofNullable(pending).orElseGet(Lists::newArrayList);
    }

    public List<AggregationTransaction> getTinkTransactions() {
        List<AggregationTransaction> transactions = new ArrayList<>();
        if (booked != null) {
            transactions.addAll(
                    booked.stream()
                            .map(transactionEntity -> transactionEntity.toTinkTransaction(false))
                            .collect(Collectors.toList()));
        }
        if (pending != null) {
            transactions.addAll(
                    pending.stream()
                            .map(transactionEntity -> transactionEntity.toTinkTransaction(true))
                            .collect(Collectors.toList()));
        }
        return transactions;
    }
}
