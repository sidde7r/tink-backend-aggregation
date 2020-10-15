package se.tink.backend.aggregation.agents.nxgen.no.openbanking.norwegian.fetcher.account.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.norwegian.fetcher.account.entities.transactions.LinksEntity;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.norwegian.fetcher.account.entities.transactions.TransactionsEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class TransactionsResponse {

    @JsonProperty("_links")
    private LinksEntity linksEntity;

    private TransactionsEntity transactions;

    public Collection<Transaction> getTinkTransactions() {
        return Stream.concat(
                        Optional.ofNullable(transactions.getBooked())
                                .orElse(Collections.emptyList()).stream()
                                .map(transaction -> transaction.toTinkTransactions(false)),
                        Optional.ofNullable(transactions.getPending())
                                .orElse(Collections.emptyList()).stream()
                                .map(transaction -> transaction.toTinkTransactions(true)))
                .collect(Collectors.toList());
    }

    public boolean hasMorePages() {
        return linksEntity.getNextEntity() != null;
    }
}
