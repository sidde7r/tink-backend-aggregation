package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.fetcher.entities.CardTransactionsEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.fetcher.entities.LinkEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class FetchCardTransactionsResponse implements TransactionKeyPaginatorResponse<String> {

    private CardTransactionsEntity cardTransactions;

    @JsonProperty("_links")
    private LinkEntity links;

    public String getNextLink() {
        return Optional.ofNullable(links)
                .map(LinkEntity::getHref)
                .orElse(
                        Optional.ofNullable(cardTransactions)
                                .map(CardTransactionsEntity::getNextLink)
                                .orElse(null));
    }

    @Override
    @JsonIgnore
    public String nextKey() {
        return getNextLink();
    }

    @Override
    @JsonIgnore
    public Collection<? extends Transaction> getTinkTransactions() {
        return cardTransactions.toTinkTransactions();
    }

    @Override
    @JsonIgnore
    public Optional<Boolean> canFetchMore() {
        return Optional.of(!Objects.isNull(getNextLink()));
    }
}
