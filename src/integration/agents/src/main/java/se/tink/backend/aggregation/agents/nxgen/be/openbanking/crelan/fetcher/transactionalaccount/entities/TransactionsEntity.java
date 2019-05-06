package se.tink.backend.aggregation.agents.nxgen.be.openbanking.crelan.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

    @JsonProperty("_links")
    private TransactionsLinksEntity links;

    private List<TransactionEntity> booked;
    private List<TransactionEntity> pending;

    @JsonIgnore
    public Collection<? extends Transaction> toTinkTransactions() {
        return Stream.concat(
                        Optional.ofNullable(booked).orElse(Collections.emptyList()).stream()
                                .map(TransactionEntity::toBookedTinkTransaction),
                        Optional.ofNullable(pending).orElse(Collections.emptyList()).stream()
                                .map(TransactionEntity::toPendingTinkTransaction))
                .collect(Collectors.toList());
    }
}
