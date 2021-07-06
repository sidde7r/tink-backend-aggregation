package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbalticsbase.fetcher.transactionalaccount.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbalticsbase.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbalticsbase.fetcher.transactionalaccount.entities.TransactionPaginationLinksEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbalticsbase.fetcher.transactionalaccount.entities.TransactionsEntity;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class TransactionsResponse {

    private AccountEntity account;

    private TransactionsEntity transactions;

    @JsonProperty("_links")
    private TransactionPaginationLinksEntity links;

    public TransactionPaginationLinksEntity getLinks() {
        return links;
    }

    public List<Transaction> getTinkTransactions() {
        return Stream.of(transactions.getPendingTransactions(), transactions.getTransactions())
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }
}
