package se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.fetcher.transactionalaccount.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.fetcher.transactionalaccount.entity.transaction.EmbeddedEntity;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.fetcher.transactionalaccount.entity.transaction.LinksEntity;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.fetcher.transactionalaccount.entity.transaction.TransactionEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class FetchTransactionsResponse implements TransactionKeyPaginatorResponse<String> {

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

    @Override
    public String nextKey() {
        return embedded.getNextPageKey();
    }

    @Override
    public Collection<? extends Transaction> getTinkTransactions() {
        if (embedded.getTransactions() == null) {
            return Collections.emptyList();
        }

        return embedded.getTransactions().stream()
                .map(TransactionEntity::toTinkTransaction)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        return Optional.of(
                embedded.getNextPageKey() != null
                        && CollectionUtils.isNotEmpty(embedded.getTransactions()));
    }
}
