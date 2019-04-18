package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.fetcher.entities.LinksEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.fetcher.entities.TransactionEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.http.URL;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.OpBankConstants.Urls.BASE_URL;

@JsonObject
public class GetTransactionsResponse implements TransactionKeyPaginatorResponse<URL> {

    private List<TransactionEntity> transactions;

    @JsonProperty("_links")
    private LinksEntity links;

    public List<TransactionEntity> getTransactions() {
        return transactions;
    }

    public LinksEntity getLinks() {
        return links;
    }

    @Override
    public URL nextKey() {
        if (getLinks().getNext() == null) {
            return null;
        }

        // TODO not tested pagination
        return new URL(BASE_URL + getLinks().getNext().getHref());
    }

    @Override
    public Collection<? extends Transaction> getTinkTransactions() {
        return transactions.stream().map(TransactionEntity::toTinkTransaction).collect(Collectors.toList());
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        return Optional.of(nextKey() != null);
    }
}
