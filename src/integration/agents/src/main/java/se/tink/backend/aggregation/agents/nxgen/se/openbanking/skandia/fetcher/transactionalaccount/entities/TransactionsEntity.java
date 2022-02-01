package se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
@Getter
@Setter
public class TransactionsEntity {

    private List<TransactionEntity> booked;
    private List<TransactionEntity> pending;

    @JsonProperty("_links")
    private TransactionLinksEntity links;

    public Collection<Transaction> toTinkTransactions() {
        return Optional.ofNullable(booked).orElse(Collections.emptyList()).stream()
                .map(TransactionEntity::toBookedTinkTransaction)
                .collect(Collectors.toList());
    }
}
