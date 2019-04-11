package se.tink.backend.aggregation.agents.nxgen.be.openbanking.ing.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collection;
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

    @JsonIgnore private Function<String, FetchTransactionsResponse> fetchNextFunction;

    @Override
    public Collection<? extends Transaction> getTinkTransactions() {
        return Optional.ofNullable(transactions)
                .map(TransactionsEntity::toTinkTransactions)
                .map(ts -> Stream.concat(ts, fetchNext()))
                .orElse(Stream.empty())
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        return Optional.empty();
    }

    public FetchTransactionsResponse setFetchNextFunction(
            Function<String, FetchTransactionsResponse> consumer) {
        fetchNextFunction = consumer;
        return this;
    }

    private Stream<? extends Transaction> fetchNext() {
        return hasNextLink()
                ? fetchNextFunction.apply(links.getHref()).setFetchNextFunction(fetchNextFunction)
                        .getTinkTransactions().stream()
                : Stream.empty();
    }

    private boolean hasNextLink() {
        return links != null && !Strings.isNullOrEmpty(links.getHref());
    }
}
