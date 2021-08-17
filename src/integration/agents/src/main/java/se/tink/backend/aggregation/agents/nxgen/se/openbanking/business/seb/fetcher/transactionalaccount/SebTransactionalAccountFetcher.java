package se.tink.backend.aggregation.agents.nxgen.se.openbanking.business.seb.fetcher.transactionalaccount;

import java.util.Collection;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.business.seb.SebConstants;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.business.seb.SebSEBusinessApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.business.seb.fetcher.transactionalaccount.rpc.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebCommonConstants;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class SebTransactionalAccountFetcher implements AccountFetcher<TransactionalAccount> {

    private final SebSEBusinessApiClient apiClient;

    public SebTransactionalAccountFetcher(SebSEBusinessApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        return apiClient.fetchAccounts().toTinkAccounts();
    }

    public FetchTransactionsResponse fetchTransactions(TransactionalAccount account, String key) {
        URL url =
                Optional.ofNullable(key)
                        .map(k -> new URL(SebConstants.Urls.BASE_AIS).concat(k))
                        .orElse(
                                new URL(SebConstants.Urls.TRANSACTIONS)
                                        .parameter(
                                                SebCommonConstants.IdTags.ACCOUNT_ID,
                                                account.getApiIdentifier()));

        return apiClient.fetchTransactions(url.toString(), key == null);
    }
}
