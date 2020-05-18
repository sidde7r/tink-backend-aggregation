package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.fetcher.transactionalaccount.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.SocieteGeneraleConstants;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.fetcher.transactionalaccount.entities.LinksTransactionsEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.fetcher.transactionalaccount.entities.TransactionsItemEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@JsonObject
public class TransactionsResponse implements TransactionKeyPaginatorResponse<URL> {

    @JsonProperty("_links")
    private LinksTransactionsEntity linksTransactionsEntity;

    private List<TransactionsItemEntity> transactions;

    @Override
    public Collection<? extends Transaction> getTinkTransactions() {
        return Optional.ofNullable(transactions).orElse(Collections.emptyList()).stream()
                .map(TransactionsItemEntity::toTinkTransactions)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        return Optional.of(linksTransactionsEntity.getNextTransactionEntity() != null);
    }

    @Override
    public URL nextKey() {
        return new URL(
                SocieteGeneraleConstants.Urls.AIS_BASE_URL
                        + linksTransactionsEntity.getNextTransactionEntity().getHref());
    }
}
