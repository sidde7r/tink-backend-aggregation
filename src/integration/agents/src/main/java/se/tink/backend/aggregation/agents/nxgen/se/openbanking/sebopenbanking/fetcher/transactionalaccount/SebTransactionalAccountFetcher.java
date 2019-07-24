package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.fetcher.transactionalaccount;

import java.util.Collection;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.SebAccountsAndCardsApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.SebAccountsAndCardsConstants;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.fetcher.transactionalaccount.rpc.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebCommonConstants;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.URL;

public class SebTransactionalAccountFetcher
        implements AccountFetcher<TransactionalAccount>,
                TransactionKeyPaginator<TransactionalAccount, String> {

    private final SebAccountsAndCardsApiClient apiClient;

    public SebTransactionalAccountFetcher(SebAccountsAndCardsApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        return apiClient.fetchAccounts().toTinkAccounts();
    }

    @Override
    public TransactionKeyPaginatorResponse<String> getTransactionsFor(
            TransactionalAccount account, String key) {

        return fetchTransactions(account, key);
    }

    public FetchTransactionsResponse fetchTransactions(TransactionalAccount account, String key) {

        URL url =
                Optional.ofNullable(key)
                        .map(k -> new URL(SebAccountsAndCardsConstants.Urls.BASE_AIS + k))
                        .orElse(
                                new URL(SebAccountsAndCardsConstants.Urls.TRANSACTIONS)
                                        .parameter(
                                                SebCommonConstants.IdTags.ACCOUNT_ID,
                                                account.getApiIdentifier()));

        return apiClient.fetchTransactions(url.toString(), key == null);
    }
}
