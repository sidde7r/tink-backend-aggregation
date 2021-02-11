package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.creditcards.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Data;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.Sparebank1Constants.Keys;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.entities.LinkEntity;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.creditcards.entity.CreditCardTransactionEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.CreditCardTransaction;

@JsonObject
@Data
public class CreditCardTransactionsResponse implements TransactionKeyPaginatorResponse<String> {
    private List<CreditCardTransactionEntity> transactions;

    @JsonProperty("_links")
    private HashMap<String, LinkEntity> links;

    @Override
    public Collection<CreditCardTransaction> getTinkTransactions() {
        return transactions.stream()
                .map(CreditCardTransactionEntity::toTinkTransaction)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        return Optional.of(links.containsKey(Keys.MORE_TRANSACTIONS_KEY));
    }

    @Override
    public String nextKey() {
        if (canFetchMore().isPresent()) {
            return links.get(Keys.MORE_TRANSACTIONS_KEY).getHref();
        }

        return null;
    }
}
