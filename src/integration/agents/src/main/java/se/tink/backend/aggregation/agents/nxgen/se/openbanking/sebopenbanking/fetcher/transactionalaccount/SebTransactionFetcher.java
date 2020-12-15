package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.fetcher.transactionalaccount;

import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.SebApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.SebConstants;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.fetcher.transactionalaccount.entities.TransactionPaginationLinksEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.fetcher.transactionalaccount.rpc.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebCommonConstants;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class SebTransactionFetcher
        implements TransactionKeyPaginator<TransactionalAccount, String> {

    private final SebApiClient apiClient;

    public SebTransactionFetcher(SebApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public TransactionKeyPaginatorResponse<String> getTransactionsFor(
            TransactionalAccount account, String key) {
        FetchTransactionsResponse transactionsResponse =
                apiClient.fetchTransactions(
                        getTransactionUrl(key, account.getApiIdentifier()).toString(), key == null);

        return new TransactionKeyPaginatorResponseImpl<>(
                transactionsResponse.getTinkTransactions(apiClient),
                nextKey(transactionsResponse.getLinks()));
    }

    private String nextKey(TransactionPaginationLinksEntity links) {
        return links != null ? links.getNext() : null;
    }

    private URL getTransactionUrl(String key, String accountApiIdentifier) {
        return Optional.ofNullable(key)
                .map(k -> new URL(SebConstants.Urls.BASE_AIS).concat(k))
                .orElse(
                        new URL(SebConstants.Urls.TRANSACTIONS)
                                .parameter(
                                        SebCommonConstants.IdTags.ACCOUNT_ID,
                                        accountApiIdentifier));
    }
}
