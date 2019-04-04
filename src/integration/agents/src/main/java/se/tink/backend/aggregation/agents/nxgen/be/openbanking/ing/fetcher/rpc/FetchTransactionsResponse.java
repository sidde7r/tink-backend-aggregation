package se.tink.backend.aggregation.agents.nxgen.be.openbanking.ing.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.assertj.core.util.Strings;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.ing.fetcher.entities.LinkEntity;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.ing.fetcher.entities.TransactionsEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class FetchTransactionsResponse implements PaginatorResponse {

    private TransactionsEntity transactions;

    @JsonProperty("_links")
    private LinkEntity links;

    @JsonIgnore private Function<String, FetchTransactionsResponse> fetchNextConsumer;

    @Override
    public Collection<? extends Transaction> getTinkTransactions() {
        return transactions != null ? Stream.concat(transactions.toTinkTransactions(),
            hasNextLink() ? fetchNext() : Stream.empty())
            .collect(Collectors.toList())
            : Collections.emptyList();
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        return Optional.empty();
    }

    public void setFetchNextConsumer(Function<String, FetchTransactionsResponse> consumer) {
        this.fetchNextConsumer = consumer;
    }

    private Stream<? extends Transaction> fetchNext() {
        return fetchNextConsumer.apply(links.getHref()).getTinkTransactions().stream();
    }

    private boolean hasNextLink() {
        return links != null && !Strings.isNullOrEmpty(links.getHref());
    }
}
