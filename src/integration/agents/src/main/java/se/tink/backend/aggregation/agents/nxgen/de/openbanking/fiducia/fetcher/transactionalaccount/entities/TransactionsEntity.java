package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class TransactionsEntity {

    @JsonProperty("_links")
    private LinksEntity links;

    private List<TransactionEntity> booked;

    public Collection<? extends Transaction> toTinkTransactions() {
        return Optional.ofNullable(booked).orElse(Collections.emptyList()).stream()
                .map(TransactionEntity::toBookedTransaction)
                .collect(Collectors.toList());
    }
}
