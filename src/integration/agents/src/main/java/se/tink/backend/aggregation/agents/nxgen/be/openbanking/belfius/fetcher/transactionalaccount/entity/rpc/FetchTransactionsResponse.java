package se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.fetcher.transactionalaccount.entity.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.fetcher.transactionalaccount.entity.transaction.Embedded;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.fetcher.transactionalaccount.entity.transaction.Links;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.libraries.amount.Amount;

@JsonObject
public class FetchTransactionsResponse implements TransactionKeyPaginatorResponse<URL> {

    @JsonProperty("_embedded")
    private Embedded embedded;

    @JsonProperty("_links")
    private Links links;

    public Embedded getEmbedded() {
        return embedded;
    }

    public void setEmbedded(Embedded embedded) {
        this.embedded = embedded;
    }

    public Links getLinks() {
        return links;
    }

    public void setLinks(Links links) {
        this.links = links;
    }

    @Override
    public URL nextKey() {
        return new URL(links.getNext().getHref());
    }

    @Override
    public Collection<? extends Transaction> getTinkTransactions() {
        return embedded.getTransactions().stream()
                .map(
                        transaction ->
                                Transaction.builder()
                                        .setDescription(transaction.getRemittanceInfo())
                                        .setAmount(
                                                new Amount(
                                                        transaction.getCurrency(),
                                                        transaction.getAmount()))
                                        .setDate(transaction.getExecutionDate())
                                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        // TODO: API pagination doesn't work
        return Optional.of(false);
    }
}
