package se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.fetcher;

import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.KbcApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.KbcConstants;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.KbcConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.BerlinGroupTransactionFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class KbcTransactionFetcher extends BerlinGroupTransactionFetcher {
    protected final KbcApiClient apiClient;

    public KbcTransactionFetcher(final KbcApiClient apiClient) {
        super(apiClient);
        this.apiClient = apiClient;
    }

    @Override
    public TransactionKeyPaginatorResponse<String> getTransactionsFor(
            TransactionalAccount account, String nextUrl) {
        final String url =
                Strings.isNullOrEmpty(nextUrl)
                        ? new URL(Urls.BASE_URL + Urls.TRANSACTIONS)
                                .parameter(
                                        KbcConstants.IdTags.ACCOUNT_ID, account.getApiIdentifier())
                                .toString()
                        : Urls.BASE_URL + nextUrl;
        return apiClient.fetchTransactions(url);
    }
}
