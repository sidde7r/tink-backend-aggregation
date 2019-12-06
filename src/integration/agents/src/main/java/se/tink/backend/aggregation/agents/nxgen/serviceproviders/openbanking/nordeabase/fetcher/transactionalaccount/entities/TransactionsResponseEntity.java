package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class TransactionsResponseEntity {

    @JsonProperty("_links")
    private List<LinkEntity> links;

    private List<TransactionEntity> transactions;

    public Collection<? extends Transaction> toTinkTransactions() {
        return Optional.ofNullable(transactions).orElse(Collections.emptyList()).stream()
                .map(TransactionEntity::toTinkTransaction)
                .collect(Collectors.toList());
    }

    public String nextKey() {
        return Optional.ofNullable(links).orElse(Collections.emptyList()).stream()
                .filter(link -> link.getRel().equalsIgnoreCase("next"))
                .findFirst()
                .map(LinkEntity::getHref)
                .orElse(null);
    }
}
