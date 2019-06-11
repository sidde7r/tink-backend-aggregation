package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.validation.Valid;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity.TransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity.TransactionsLinksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.http.URL;

@JsonObject
public class FetchTransactionsResponse implements TransactionKeyPaginatorResponse<URL> {
    @JsonProperty("transactions")
    @Valid
    private List<TransactionEntity> transactions = new ArrayList<TransactionEntity>();

    @JsonProperty("_links")
    private TransactionsLinksEntity links = null;

    @JsonIgnore private TransactionalAccount transactionalAccount;

    public List<TransactionEntity> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<TransactionEntity> transactions) {
        this.transactions = transactions;
    }

    public TransactionsLinksEntity getLinks() {
        return links;
    }

    public void setLinks(TransactionsLinksEntity links) {
        this.links = links;
    }

    @Override
    public URL nextKey() {
        return new URL(links.getNext().getHref());
    }

    @Override
    public Collection<? extends Transaction> getTinkTransactions() {
        return transactions.stream()
                .map(transactionEntity -> transactionEntity.toTinkTransaction(transactionalAccount))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        return Optional.of(
                links.getNext() != null
                        && !links.getNext().getHref().equalsIgnoreCase(links.getSelf().getHref()));
    }

    public void setTransactionalAccount(TransactionalAccount transactionalAccount) {
        this.transactionalAccount = transactionalAccount;
    }
}
