package se.tink.backend.aggregation.agents.nxgen.dk.openbanking.bec.fetcher.transactionalaccount.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.dk.openbanking.bec.fetcher.transactionalaccount.entities.LinksEntity;
import se.tink.backend.aggregation.agents.nxgen.dk.openbanking.bec.fetcher.transactionalaccount.entities.TransactionsEntity;
import se.tink.backend.aggregation.agents.nxgen.ro.banks.raiffeisen.fetcher.entity.AccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

import java.util.Collection;
import java.util.Optional;

@JsonObject
public class GetTransactionsResponse implements PaginatorResponse {

    @JsonProperty("_links")
    private LinksEntity links;

    private AccountEntity account;
    private TransactionsEntity transactions;

    @Override
    public Collection<? extends Transaction> getTinkTransactions() {
        return transactions.getTinkTransactions();
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        return Optional.empty();
    }
}
