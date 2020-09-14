package se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class TransactionEntity {
    private final List<BookedTransactionEntity> booked = Collections.emptyList();
    private final List<PendingTransactionEntity> pending = Collections.emptyList();

    public Collection<Transaction> toTinkTransactions() {
        return Stream.concat(
                        mapToTinkTransactionStream(booked), mapToTinkTransactionStream(pending))
                .collect(Collectors.toList());
    }

    private Stream<Transaction> mapToTinkTransactionStream(
            List<? extends TransactionDetailsEntity> list) {
        return list.stream().map(TransactionDetailsEntity::toTinkTransaction);
    }
}
