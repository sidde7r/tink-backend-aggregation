package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Data;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity.TransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity.TransactionsLinksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@JsonObject
@Data
public class FetchTransactionsResponse implements TransactionKeyPaginatorResponse<URL> {

    private List<TransactionEntity> transactions;

    @JsonProperty("_links")
    private TransactionsLinksEntity links;

    @Override
    public URL nextKey() {
        return new URL(links.getNext().getHref());
    }

    @Override
    public Collection<? extends Transaction> getTinkTransactions() {
        return transactions.stream()
                .map(TransactionEntity::toTinkTransaction)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        return Optional.of(
                links.getNext() != null
                        && !links.getNext().getHref().equalsIgnoreCase(links.getSelf().getHref()));
    }
}
