
package se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.fetcher.transactionalaccount.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.fetcher.transactionalaccount.entity.transaction.Links;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.fetcher.transactionalaccount.entity.transaction.Transaction;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.URL;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@JsonObject
public class GetTransactionsResponse implements TransactionKeyPaginatorResponse<URL> {

    @JsonProperty("_links")
    private Links links;

    private List<Transaction> transactions;

    public Links getLinks() {
        return links;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public TransactionalAccount account;

    @Override
    public URL nextKey() {
        return new URL(links.getNext().getHref());
    }

    @Override
    public Collection<? extends se.tink.backend.aggregation.nxgen.core.transaction.Transaction> getTinkTransactions() {
        return transactions.stream().map(item -> item.toTinkModel(account))
            .collect(Collectors.toList());
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        return Optional.of(links.getNext() != null);
    }

    public TransactionalAccount getAccount() {
        return account;
    }

    public void setAccount(TransactionalAccount account) {
        this.account = account;
    }
}
