package se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken;

import java.util.Date;
import java.util.UUID;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.IcaBankenConstants.Account;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.IcaBankenConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.IcaBankenConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.IcaBankenConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.IcaBankenConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.configuration.IcaBankenConfiguration;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.fetcher.transactionalaccount.rpc.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.fetcher.transactionalaccount.rpc.FetchTransactionsResponse;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.date.ThreadSafeDateFormat;

public final class IcaBankenApiClient {

    private final TinkHttpClient client;
    private final SessionStorage sessionStorage;
    private IcaBankenConfiguration configuration;

    public IcaBankenApiClient(TinkHttpClient client, SessionStorage sessionStorage) {
        this.client = client;
        this.sessionStorage = sessionStorage;
    }

    private RequestBuilder createRequest(URL url) {
        return client.request(url)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON);
    }

    public FetchAccountsResponse fetchAccounts() {
        final URL baseUrl = new URL(configuration.getBaseUrl());
        final URL requestUrl = baseUrl.concatWithSeparator(Urls.ACCOUNTS_PATH);

        return createRequest(requestUrl)
                .queryParam(QueryKeys.WITH_BALANCE, "true")
                .get(FetchAccountsResponse.class);
    }

    public FetchTransactionsResponse fetchTransactionsForAccount(
            String apiIdentifier, Date fromDate, Date toDate) {
        final URL baseUrl = new URL(configuration.getBaseUrl());
        final URL requestUrl =
                baseUrl.concatWithSeparator(Urls.TRANSACTIONS_PATH)
                        .parameter(Account.ACCOUNT_ID, apiIdentifier);

        return createRequest(requestUrl)
                .queryParam(
                        QueryKeys.DATE_FROM, ThreadSafeDateFormat.FORMATTER_DAILY.format(fromDate))
                .queryParam(QueryKeys.DATE_TO, ThreadSafeDateFormat.FORMATTER_DAILY.format(toDate))
                .queryParam(QueryKeys.STATUS, QueryValues.STATUS)
                .header(HeaderKeys.REQUEST_ID, UUID.randomUUID().toString())
                .get(FetchTransactionsResponse.class);
    }

    public void setConfiguration(IcaBankenConfiguration icaBankenConfiguration) {
        this.configuration = icaBankenConfiguration;
    }
}
