package se.tink.backend.aggregation.agents.nxgen.no.openbanking.sparebank1.fetcher.transactionalaccount;

import se.tink.backend.aggregation.agents.nxgen.no.openbanking.sparebank1.SpareBank1ApiClient;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@JsonObject
public class SpareBank1TransactionalAccountFetcher
        implements AccountFetcher<TransactionalAccount>, TransactionFetcher<TransactionalAccount> {
    private final SpareBank1ApiClient apiClient;

    public SpareBank1TransactionalAccountFetcher(SpareBank1ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        return apiClient.getAccounts().toTinkAccounts();
    }

    @Override
    public List<AggregationTransaction> fetchTransactionsFor(TransactionalAccount account) {
        return new ArrayList<>(apiClient.getTransactions(account).getTinkTransactions());
    }
}
