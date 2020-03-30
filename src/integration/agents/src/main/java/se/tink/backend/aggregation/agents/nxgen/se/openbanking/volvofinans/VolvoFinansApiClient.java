package se.tink.backend.aggregation.agents.nxgen.se.openbanking.volvofinans;

import java.util.Calendar;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.volvofinans.VolvoFinansConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.volvofinans.VolvoFinansConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.volvofinans.VolvoFinansConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.volvofinans.VolvoFinansConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.volvofinans.VolvoFinansConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.volvofinans.VolvoFinansConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.volvofinans.VolvoFinansConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.volvofinans.authenticator.rpc.TokenRequest;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.volvofinans.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.volvofinans.configuration.VolvoFinansConfiguration;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.volvofinans.fetcher.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.volvofinans.fetcher.transactionalaccount.rpc.TransactionsResponse;
import se.tink.backend.aggregation.configuration.eidas.proxy.EidasProxyConfiguration;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.date.DateFormat;
import se.tink.libraries.date.DateFormat.Zone;

public final class VolvoFinansApiClient {

    private final TinkHttpClient client;
    private final PersistentStorage persistentStorage;
    private VolvoFinansConfiguration configuration;

    public VolvoFinansApiClient(TinkHttpClient client, PersistentStorage persistentStorage) {
        this.client = client;
        this.persistentStorage = persistentStorage;
    }

    private VolvoFinansConfiguration getConfiguration() {
        return Optional.ofNullable(configuration)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    protected void setConfiguration(
            VolvoFinansConfiguration configuration,
            EidasProxyConfiguration eidasProxyConfiguration) {
        this.configuration = configuration;
        this.client.setEidasProxy(eidasProxyConfiguration);
    }

    public URL getAuthorizeUrl(String state) {
        final String clientId = getConfiguration().getClientId();
        final String redirectUri = getConfiguration().getRedirectUrl();

        return createRequest(Urls.AUTH)
                .queryParam(QueryKeys.CLIENT_ID, clientId)
                .queryParam(QueryKeys.RESPONSE_TYPE, QueryValues.RESPONSE_TYPE)
                .queryParam(QueryKeys.REDIRECT_URI, redirectUri)
                .queryParam(QueryKeys.SCOPE, QueryValues.SCOPE)
                .queryParam(QueryKeys.STATE, state)
                .getUrl();
    }

    public OAuth2Token getToken(String code) {
        final String redirectUri = getConfiguration().getRedirectUrl();
        final String clientId = getConfiguration().getClientId();
        final String clientSecret = getConfiguration().getClientSecret();

        TokenRequest request =
                new TokenRequest(
                        FormValues.AUTHORIZATION_CODE, code, redirectUri, clientId, clientSecret);

        return client.request(Urls.TOKEN)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON)
                .post(TokenResponse.class, request.toData())
                .toTinkToken();
    }

    public AccountsResponse fetchAccounts() {
        final String requestId = UUID.randomUUID().toString();
        final String apiKey = configuration.getClientId();

        return client.request(Urls.ACCOUNTS)
                .accept(MediaType.APPLICATION_JSON)
                .queryParam(QueryKeys.WITH_BALANCE, QueryValues.TRUE)
                .header(HeaderKeys.X_REQUEST_ID, requestId)
                .header(HeaderKeys.X_API_KEY, apiKey)
                .addBearerToken(getTokenFromStorage())
                .get(AccountsResponse.class);
    }

    public TransactionsResponse fetchTransactions(
            TransactionalAccount account, Date startDate, Date endDate) {
        final String apiKey = configuration.getClientId();
        final String requestId = UUID.randomUUID().toString();

        return client.request(
                        Urls.TRANSACTIONS.parameter(
                                VolvoFinansConstants.IdTags.ACCOUNT_ID,
                                account.getFromTemporaryStorage(StorageKeys.ACCOUNT_ID)))
                .queryParam(
                        QueryKeys.DATE_TO,
                        DateFormat.formatDateTime(endDate, DateFormat.YEAR_MONTH_DAY, Zone.UTC))
                .queryParam(
                        QueryKeys.DATE_FROM,
                        DateFormat.formatDateTime(
                                get90DaysDate(endDate), DateFormat.YEAR_MONTH_DAY, Zone.UTC))
                .header(HeaderKeys.X_API_KEY, apiKey)
                .header(HeaderKeys.X_REQUEST_ID, requestId)
                .accept(MediaType.APPLICATION_JSON)
                .addBearerToken(getTokenFromStorage())
                .get(TransactionsResponse.class);
    }

    private RequestBuilder createRequest(URL url) {
        return client.request(url)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON);
    }

    private OAuth2Token getTokenFromStorage() {
        return persistentStorage
                .get(StorageKeys.OAUTH_TOKEN, OAuth2Token.class)
                .orElseThrow(
                        () -> new IllegalStateException(SessionError.SESSION_EXPIRED.exception()));
    }

    private static Date get90DaysDate(Date toDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(toDate);
        calendar.add(Calendar.DATE, -89);
        return calendar.getTime();
    }
}
