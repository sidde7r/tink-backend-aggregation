package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.akita;

import javax.ws.rs.core.MediaType;

import com.amazonaws.codegen.model.service.Http;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.akita.fetcher.transactionalaccount.rpc.GetAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.akita.fetcher.transactionalaccount.rpc.GetTransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.date.ThreadSafeDateFormat;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public final class AkitaApiClient {
    private final TinkHttpClient client;
    private final SessionStorage sessionStorage;
    private final PersistentStorage persistentStorage;

    public AkitaApiClient(
            TinkHttpClient client,
            SessionStorage sessionStorage,
            PersistentStorage persistentStorage) {
        this.client = client;
        this.sessionStorage = sessionStorage;
        this.persistentStorage = persistentStorage;
    }

    private RequestBuilder createRequest(URL url) {
        return client.request(url)
                .header(AkitaConstants.HeaderKeys.X_REQUEST_ID, UUID.randomUUID())
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON);
    }

    private RequestBuilder createRequestInSession(URL url) {
        return createRequest(url)
                .header(
                        AkitaConstants.HeaderKeys.CONSENT_ID,
                        persistentStorage.get(AkitaConstants.StorageKeys.CONSENT_ID))
                .header(
                        AkitaConstants.HeaderKeys.X_IBM_CLIENT_ID,
                        persistentStorage.get(AkitaConstants.StorageKeys.CLIENT_ID))
                .header(
                        AkitaConstants.HeaderKeys.X_IBM_CLIENT_SECRET,
                        persistentStorage.get(AkitaConstants.StorageKeys.CLIENT_SECRET));
    }

    public List<TransactionalAccount> getAccounts() {
        return createRequestInSession(new URL(AkitaConstants.Urls.GET_ACCOUNTS))
                .queryParam(AkitaConstants.QueryKeys.WITH_BALANCE, AkitaConstants.QueryValues.TRUE)
                .get(GetAccountsResponse.class)
                .toTinkAccounts();
    }

    public PaginatorResponse getTransactions(
            TransactionalAccount account, Date fromDate, Date toDate) {
        return createRequestInSession(
                        new URL(AkitaConstants.Urls.GET_TRANSACTIONS)
                                .parameter(
                                        AkitaConstants.IdTags.ACCOUNT_ID,
                                        account.getApiIdentifier()))
                .queryParam(
                        AkitaConstants.QueryKeys.DATE_FROM,
                        ThreadSafeDateFormat.FORMATTER_DAILY.format(fromDate))
                .queryParam(
                        AkitaConstants.QueryKeys.DATE_TO,
                        ThreadSafeDateFormat.FORMATTER_DAILY.format(toDate))
                .queryParam(
                        AkitaConstants.QueryKeys.BOOKING_STATUS, AkitaConstants.QueryValues.BOTH)
                .get(GetTransactionsResponse.class);
    }
}
