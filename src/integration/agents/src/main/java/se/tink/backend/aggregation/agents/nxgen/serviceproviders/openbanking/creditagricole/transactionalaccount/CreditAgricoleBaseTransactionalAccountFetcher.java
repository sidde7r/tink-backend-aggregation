package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount;

import java.util.Collection;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.apiclient.CreditAgricoleBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.rpc.GetAccountsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;

public class CreditAgricoleBaseTransactionalAccountFetcher
        implements AccountFetcher<TransactionalAccount>, TransactionFetcher<TransactionalAccount> {

    private final CreditAgricoleBaseApiClient apiClient;

    public CreditAgricoleBaseTransactionalAccountFetcher(CreditAgricoleBaseApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        GetAccountsResponse getAccountsResponse = apiClient.getAccounts();

        if (getAccountsResponse.ifNecessaryConsents()) {
            apiClient.putConsents(getAccountsResponse.getListOfNecessaryConstents());
            getAccountsResponse = apiClient.getAccounts();
        }

        return getAccountsResponse.toTinkAccounts();
    }

    @Override
    public List<AggregationTransaction> fetchTransactionsFor(TransactionalAccount account) {
        return apiClient.getTransactions(account.getApiIdentifier()).toTinkTransactions();
    }
}
