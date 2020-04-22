package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.fetcher.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.fetcher.entities.LinkEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.fetcher.entities.TransactionsEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class BaseFetchTransactionsResponse<T extends TransactionsEntity>
        implements TransactionKeyPaginatorResponse<String> {

    private T transactions;

    private AccountEntity account;

    @JsonProperty("_links")
    private LinkEntity links;

    public TransactionsEntity getTransactions() {
        return transactions;
    }

    public String getNextLink() {
        return Optional.ofNullable(links)
                .map(LinkEntity::getHref)
                .orElse(
                        Optional.ofNullable(transactions)
                                .map(TransactionsEntity::getNextLink)
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
        return transactions.toTinkTransactions();
    }

    @Override
    @JsonIgnore
    public Optional<Boolean> canFetchMore() {
        return Optional.of(!Objects.isNull(getNextLink()));
    }
}
