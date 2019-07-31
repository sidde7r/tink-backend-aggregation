package se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.fetcher.transactionalaccount.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collection;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.fetcher.transactionalaccount.entity.transaction.EmbeddedEntity;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.fetcher.transactionalaccount.entity.transaction.LinksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class FetchTransactionsResponse {

    @JsonProperty("_embedded")
    private EmbeddedEntity embedded;

    @JsonProperty("_links")
    private LinksEntity links;

    public EmbeddedEntity getEmbedded() {
        return embedded;
    }

    public void setEmbedded(EmbeddedEntity embedded) {
        this.embedded = embedded;
    }

    public LinksEntity getLinks() {
        return links;
    }

    public void setLinks(LinksEntity links) {
        this.links = links;
    }

    @JsonIgnore
    public Collection<? extends Transaction> toTinkTransactions() {
        return embedded.getTransactions().stream()
                .map(
                        transaction ->
                                Transaction.builder()
                                        .setDescription(transaction.getRemittanceInfo())
                                        .setAmount(
                                                new ExactCurrencyAmount(
                                                        transaction.getAmount(),
                                                        transaction.getCurrency()))
                                        .setDate(transaction.getExecutionDate())
                                        .build())
                .collect(Collectors.toList());
    }
}
