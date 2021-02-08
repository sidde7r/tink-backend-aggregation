package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.collections4.ListUtils;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class TransactionsResponseEntity<T extends TransactionEntity> {

    @JsonProperty("_links")
    private List<LinkEntity> links;

    private List<T> transactions;

    public Collection<? extends Transaction> toTinkTransactions() {
        return ListUtils.emptyIfNull(transactions).stream()
                .map(T::toTinkTransaction)
                .collect(Collectors.toList());
    }

    public String nextKey() {
        return ListUtils.emptyIfNull(links).stream()
                .filter(link -> link.getRel().equalsIgnoreCase("next"))
                .findFirst()
                .map(LinkEntity::getHref)
                .orElse(null);
    }

    public List<T> getTransactions() {
        return transactions;
    }
}
