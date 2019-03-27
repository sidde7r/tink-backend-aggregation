package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.fetcher.transactionalaccount;

import java.util.Collection;
import java.util.Date;

import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.LansforsakringarApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.LansforsakringarConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.libraries.date.ThreadSafeDateFormat;

@JsonObject
public class LansforsakringarTransactionalAccountFetcher
        implements AccountFetcher<TransactionalAccount>,
                TransactionKeyPaginator<TransactionalAccount, String> {
    private final LansforsakringarApiClient apiClient;

    public LansforsakringarTransactionalAccountFetcher(LansforsakringarApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        return apiClient.getAccounts();
    }

    @Override
    public TransactionKeyPaginatorResponse<String> getTransactionsFor(
            TransactionalAccount account, String key) {

        RequestBuilder req =
                key == null
                        ? apiClient.createRequestInSession(
                        new URL(LansforsakringarConstants.Urls.GET_TRANSACTIONS)
                                .parameter(
                                        LansforsakringarConstants.IdTags.ACCOUNT_ID,
                                        account.getApiIdentifier()))
                        .queryParam(
                                LansforsakringarConstants.QueryKeys.DATE_FROM,
                                ThreadSafeDateFormat.FORMATTER_DAILY.format(new Date()))
                        : apiClient.createRequestInSession(
                        new URL(LansforsakringarConstants.Urls.BASE_URL + key));
        return apiClient.getTransactions(account, req);
    }
}
