package se.tink.backend.aggregation.agents.nxgen.dk.openbanking.bec;

import se.tink.backend.aggregation.agents.nxgen.dk.openbanking.bec.fetcher.transactionalaccount.rpc.GetAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.dk.openbanking.bec.fetcher.transactionalaccount.rpc.GetTransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.date.ThreadSafeDateFormat;

import javax.ws.rs.core.MediaType;
import java.util.Date;
import java.util.List;

public final class BecApiClient {
    private final TinkHttpClient client;
    private final SessionStorage sessionStorage;
    private final PersistentStorage persistentStorage;

    public BecApiClient(
            TinkHttpClient client,
            SessionStorage sessionStorage,
            PersistentStorage persistentStorage) {
        this.client = client;
        this.sessionStorage = sessionStorage;
        this.persistentStorage = persistentStorage;
    }

    private RequestBuilder createRequest(URL url) {
        return client.request(url)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .header(
                        BecConstants.HeaderKeys.X_IBM_CLIENT_ID,
                        persistentStorage.get(BecConstants.StorageKeys.CLIENT_ID));
    }

    public List<TransactionalAccount> getAccounts() {
        return createRequest(new URL(BecConstants.Urls.GET_ACCOUNTS))
                .queryParam(BecConstants.QueryKeys.WITH_BALANCE, BecConstants.QueryValues.TRUE)
                .get(GetAccountsResponse.class)
                .toTinkAccounts();
    }

    public PaginatorResponse getTransactions(
            TransactionalAccount account, Date fromDate, Date toDate) {
        return createRequest(
                        new URL(BecConstants.Urls.GET_TRANSACTIONS)
                                .parameter(
                                        BecConstants.IdTags.ACCOUNT_ID, account.getApiIdentifier()))
                .queryParam(BecConstants.QueryKeys.WITH_BALANCE, BecConstants.QueryValues.TRUE)
                .queryParam(BecConstants.QueryKeys.BOOKING_STATUS, BecConstants.QueryValues.BOTH)
                .queryParam(
                        BecConstants.QueryKeys.DATE_FROM,
                        ThreadSafeDateFormat.FORMATTER_DAILY.format(fromDate))
                .queryParam(
                        BecConstants.QueryKeys.DATE_TO,
                        ThreadSafeDateFormat.FORMATTER_DAILY.format(toDate))
                .get(GetTransactionsResponse.class);
    }
}
