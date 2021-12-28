package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.fetcher.transacitons.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Data;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.fetcher.transacitons.entity.TransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.fetcher.transacitons.entity.TransactionsLinkEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
@Data
public class TransactionResponse implements TransactionKeyPaginatorResponse<String> {
    private List<TransactionEntity> transactions;

    @JsonProperty("_links")
    private TransactionsLinkEntity links;

    private List<TransactionEntity> getTransactions() {
        return Optional.ofNullable(transactions).orElseGet(Collections::emptyList);
    }

    @Override
    public Collection<? extends Transaction> getTinkTransactions() {
        return getTransactions().stream()
                .map(TransactionEntity::toTinkTransaction)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        return Optional.of(links.getNext() != null);
    }

    @Override
    public String nextKey() {
        return links.getNext().getHref();
    }
}
