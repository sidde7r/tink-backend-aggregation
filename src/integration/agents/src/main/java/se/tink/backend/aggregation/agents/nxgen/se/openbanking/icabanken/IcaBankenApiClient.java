package se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken;

import java.util.Date;
import java.util.UUID;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.IcaBankenConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.fetcher.transactionalaccount.rpc.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.fetcher.transactionalaccount.rpc.FetchTransactionsResponse;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.date.ThreadSafeDateFormat;

public final class IcaBankenApiClient {

    private final TinkHttpClient client;
    private final SessionStorage sessionStorage;
    private final PersistentStorage persistentStorage;

    public IcaBankenApiClient(
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
                .type(MediaType.APPLICATION_JSON);
    }

    public FetchAccountsResponse fetchAccounts() {
        URL baseUrl = new URL(persistentStorage.get(IcaBankenConstants.StorageKeys.BASE_URL));
        URL requestUrl = baseUrl.concatWithSeparator(IcaBankenConstants.Urls.ACCOUNTS_PATH);

        return createRequest(requestUrl)
                .queryParam(IcaBankenConstants.QueryKeys.WITH_BALANCE, "true")
                .get(FetchAccountsResponse.class);
    }

    public FetchTransactionsResponse fetchTransactionsForAccount(
            String apiIdentifier, Date fromDate, Date toDate) {
        URL baseUrl = new URL(persistentStorage.get(IcaBankenConstants.StorageKeys.BASE_URL));
        URL requestUrl =
                baseUrl.concatWithSeparator(Urls.TRANSACTIONS_PATH)
                        .parameter(IcaBankenConstants.Account.ACCOUNT_ID, apiIdentifier);

        return createRequest(requestUrl)
                .queryParam(
                        IcaBankenConstants.QueryKeys.DATE_FROM,
                        ThreadSafeDateFormat.FORMATTER_DAILY.format(fromDate))
                .queryParam(
                        IcaBankenConstants.QueryKeys.DATE_TO,
                        ThreadSafeDateFormat.FORMATTER_DAILY.format(toDate))
                .queryParam(
                        IcaBankenConstants.QueryKeys.STATUS, IcaBankenConstants.QueryValues.STATUS)
                .header(IcaBankenConstants.HeaderKeys.REQUEST_ID, UUID.randomUUID().toString())
                .get(FetchTransactionsResponse.class);
    }
}
