package se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta.fetcher.transactionalaccount;

import se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta.ArgentaApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta.ArgentaConstants;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class ArgentaTransactionalAccountTransactionFetcher
        implements TransactionKeyPaginator<TransactionalAccount, String> {
    private final ArgentaApiClient apiClient;

    public ArgentaTransactionalAccountTransactionFetcher(ArgentaApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public TransactionKeyPaginatorResponse<String> getTransactionsFor(
            TransactionalAccount account, String key) {
        URL url;
        if (key == null) {
            url =
                    new URL(
                                    ArgentaConstants.Urls.BASE_BERLIN_GROUP
                                            + account.getFromTemporaryStorage(
                                                    ArgentaConstants.StorageKeys.TRANSACTIONS_URL))
                            .queryParam(
                                    ArgentaConstants.QueryKeys.DATE_FROM,
                                    ArgentaConstants.QueryValues.START_DATE)
                            .queryParam(
                                    ArgentaConstants.QueryKeys.BOOKING_STATUS,
                                    ArgentaConstants.QueryValues.BOTH);
        } else {
            url = new URL(ArgentaConstants.Urls.BASE_BERLIN_GROUP + key);
        }

        return apiClient.getTransactions(url);
    }
}
