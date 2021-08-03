package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbaltics.fetcher.transactionalaccount.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbaltics.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbaltics.fetcher.transactionalaccount.entities.TransactionPaginationLinksEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbaltics.fetcher.transactionalaccount.entities.TransactionsEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class TransactionsResponse {

    private AccountEntity account;

    private TransactionsEntity transactions;

    @JsonProperty("_links")
    private TransactionPaginationLinksEntity links;

    public TransactionPaginationLinksEntity getLinks() {
        return links;
    }

    @JsonIgnore
    public List<Transaction> getTinkTransactions(String providerMarket) {
        return Stream.of(
                        transactions.getPendingTransactions(providerMarket),
                        transactions.getTransactions(providerMarket))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }
}
