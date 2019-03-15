package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@JsonObject
public class TransactionsResponseEntity {

    @JsonProperty("_links")
    private List<LinkEntity> links;

    private List<TransactionEntity> transactions;

    public Collection<? extends Transaction> toTinkTransactions() {
        return transactions != null
                ? transactions.stream()
                        .map(TransactionEntity::toTinkTransaction)
                        .collect(Collectors.toList())
                : Collections.emptyList();
    }

    public String nextKey() {
        if (links != null) {
            List<LinkEntity> filteredLinks =
                    links.stream()
                            .filter(link -> link.getRel().equalsIgnoreCase("next"))
                            .collect(Collectors.toList());
            return filteredLinks.isEmpty() ? null : filteredLinks.get(0).getHref();
        }
        return null;
    }
}
