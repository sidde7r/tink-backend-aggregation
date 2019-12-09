package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.fetcher.transactionalaccount.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collection;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.fetcher.transactionalaccount.entities.LinksEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.fetcher.transactionalaccount.entities.TransactionsEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class GetTransactionsResponse implements PaginatorResponse {

    @JsonProperty("_links")
    private LinksEntity links;

    private AccountEntity account;
    private TransactionsEntity transactions;

    @Override
    public Collection<? extends Transaction> getTinkTransactions() {
        return Optional.ofNullable(transactions)
                .orElse(new TransactionsEntity())
                .getTinkTransactions();
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        return Optional.empty();
    }
}
