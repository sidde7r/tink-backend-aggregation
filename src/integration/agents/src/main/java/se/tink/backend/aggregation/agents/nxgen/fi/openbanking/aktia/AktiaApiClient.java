package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.configuration.AktiaConfiguration;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.fetcher.transactionalaccount.rpc.GetAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.fetcher.transactionalaccount.rpc.GetTransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.date.ThreadSafeDateFormat;

public final class AktiaApiClient {
    private final TinkHttpClient client;
    private final SessionStorage sessionStorage;
    private AktiaConfiguration configuration;

    public AktiaApiClient(TinkHttpClient client, SessionStorage sessionStorage) {
        this.client = client;
        this.sessionStorage = sessionStorage;
    }

    public AktiaConfiguration getConfiguration() {
        return Optional.ofNullable(configuration)
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        AktiaConstants.ErrorMessages.MISSING_CONFIGURATION));
    }

    public void setConfiguration(AktiaConfiguration configuration) {
        this.configuration = configuration;
    }

    private RequestBuilder createRequest(URL url) {
        return client.request(url)
                .header(AktiaConstants.HeaderKeys.X_REQUEST_ID, UUID.randomUUID())
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON);
    }

    private RequestBuilder createRequestInSession(URL url) {
        return createRequest(url)
                .header(AktiaConstants.HeaderKeys.CONSENT_ID, configuration.getConsentId())
                .header(AktiaConstants.HeaderKeys.X_IBM_CLIENT_ID, configuration.getClientId())
                .header(
                        AktiaConstants.HeaderKeys.X_IBM_CLIENT_SECRET,
                        configuration.getClientSecret());
    }

    public List<TransactionalAccount> getAccounts() {
        return createRequestInSession(AktiaConstants.Urls.GET_ACCOUNTS)
                .queryParam(AktiaConstants.QueryKeys.WITH_BALANCE, AktiaConstants.QueryValues.TRUE)
                .get(GetAccountsResponse.class)
                .toTinkAccounts();
    }

    public PaginatorResponse getTransactions(
            TransactionalAccount account, Date fromDate, Date toDate) {
        return createRequestInSession(
                        AktiaConstants.Urls.GET_TRANSACTIONS.parameter(
                                AktiaConstants.IdTags.ACCOUNT_ID, account.getApiIdentifier()))
                .queryParam(
                        AktiaConstants.QueryKeys.DATE_FROM,
                        ThreadSafeDateFormat.FORMATTER_DAILY.format(fromDate))
                .queryParam(
                        AktiaConstants.QueryKeys.DATE_TO,
                        ThreadSafeDateFormat.FORMATTER_DAILY.format(toDate))
                .queryParam(
                        AktiaConstants.QueryKeys.BOOKING_STATUS, AktiaConstants.QueryValues.BOTH)
                .get(GetTransactionsResponse.class);
    }
}
