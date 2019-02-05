package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.Sparebank1Constants;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.entities.LinkEntity;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.entities.CreditCardTransactionEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.CreditCardTransaction;

@JsonObject
public class CreditCardTransactionsResponse implements TransactionKeyPaginatorResponse<String> {
    private List<CreditCardTransactionEntity> transactions;
    private String cardNumberFormatted;
    private String productName;
    private Boolean creditCardAuthorizationsFailure;
    @JsonProperty("_links")
    private HashMap<String, LinkEntity> links;

    public List<CreditCardTransactionEntity> getCreditCardTransactions() {
        return transactions;
    }

    public String getCardNumberFormatted() {
        return cardNumberFormatted;
    }

    public String getProductName() {
        return productName;
    }

    public Boolean getCreditCardAuthorizationsFailure() {
        return creditCardAuthorizationsFailure;
    }

    public HashMap<String, LinkEntity> getLinks() {
        return links;
    }

    @Override
    public Collection<CreditCardTransaction> getTinkTransactions() {
        return transactions.stream()
                .map(CreditCardTransactionEntity::toTinkTransaction)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        return Optional.of(links.containsKey(Sparebank1Constants.Keys.MORE_TRANSACTIONS_KEY));
    }

    @Override
    public String nextKey() {
        if (canFetchMore().isPresent()) {
            return links.get(Sparebank1Constants.Keys.MORE_TRANSACTIONS_KEY).getHref();
        }

        return null;
    }
}
