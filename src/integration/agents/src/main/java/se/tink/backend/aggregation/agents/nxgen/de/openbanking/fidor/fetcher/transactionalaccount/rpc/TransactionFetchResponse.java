package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fidor.fetcher.transactionalaccount.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collection;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fidor.fetcher.transactionalaccount.entities.LinksEntity;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fidor.fetcher.transactionalaccount.entities.TransactionEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class TransactionFetchResponse implements PaginatorResponse {
    private TransactionEntity transactions;

    @JsonProperty("_links")
    private LinksEntity linksEntity;

    @Override
    public Collection<? extends Transaction> getTinkTransactions() {
        return transactions.getTransactions();
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        return Optional.of(linksEntity != null && linksEntity.hasNext());
    }
}
