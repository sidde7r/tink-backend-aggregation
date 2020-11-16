package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class TransactionsEntity {
    @JsonProperty("_link")
    private TransactionsLinksEntity links;

    private List<TransactionEntity> booked;
    private List<TransactionEntity> pending;

    public Collection<Transaction> getTinkTransactions() {
        return Stream.concat(
                        Optional.ofNullable(booked).orElse(Collections.emptyList()).stream()
                                .filter(TransactionEntity::isNotEmpty)
                                .map(TransactionEntity::toBookedTransaction),
                        Optional.ofNullable(pending).orElse(Collections.emptyList()).stream()
                                .filter(TransactionEntity::isNotEmpty)
                                .map(TransactionEntity::toPendingTransaction))
                .collect(Collectors.toList());
    }
}
