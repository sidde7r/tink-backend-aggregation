package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.rpc;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.entities.TransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.entities.TransactionsLinksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@JsonObject
public class GetTransactionsResponse implements TransactionKeyPaginatorResponse<URL> {
    @JsonProperty("_links")
    private TransactionsLinksEntity links;
    private List<TransactionEntity> transactions;

    @Override
    public URL nextKey() {
        return new URL(links.getNext().getHref());
    }

    @Override
    public Collection<? extends Transaction> getTinkTransactions() {
        return Optional.ofNullable(transactions).orElse(Collections.emptyList()).stream()
                .map(TransactionEntity::toTinkTransaction)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        return Optional.of(
                links.getNext() != null
                        && links.getSelf() != null
                        && links.getSelf().getHref() != null
                        && !links.getSelf().getHref().equalsIgnoreCase(links.getNext().getHref()));
    }
}
