package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.fetcher.rpc;

import static se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.OpBankConstants.Urls.BASE_URL;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Data;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.fetcher.entities.CreditCardTransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.fetcher.entities.LinksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@JsonObject
@Data
public class GetCreditCardTransactionsResponse implements TransactionKeyPaginatorResponse<URL> {

    private List<CreditCardTransactionEntity> transactions;

    @JsonProperty("_links")
    private LinksEntity links;

    @Override
    public URL nextKey() {
        if (links.getNext() == null) {
            return null;
        }

        return new URL(BASE_URL + links.getNext().getHref());
    }

    @Override
    public Collection<? extends Transaction> getTinkTransactions() {
        return transactions.stream()
                .map(CreditCardTransactionEntity::toTinkTransaction)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        return Optional.of(nextKey() != null);
    }
}
