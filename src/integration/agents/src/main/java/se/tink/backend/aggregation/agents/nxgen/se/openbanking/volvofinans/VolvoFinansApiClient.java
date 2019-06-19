package se.tink.backend.aggregation.agents.nxgen.se.openbanking.volvofinans;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.volvofinans.VolvoFinansConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.volvofinans.VolvoFinansConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.volvofinans.VolvoFinansConstants.Format;
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
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.volvofinans.utils.Utils;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

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

    protected void setConfiguration(VolvoFinansConfiguration configuration) {
        this.configuration = configuration;
    }

    public URL getAuthorizeUrl(String state) {
        final String clientId = getConfiguration().getClientId();
        final String clientSecret = getConfiguration().getClientSecret();
        final String redirectUri = getConfiguration().getRedirectUrl() + "?state=" + state;

        // TODO For productions use the real clientId and clientSecret instead of constants
        return createRequest(Urls.AUTH)
                .queryParam(QueryKeys.CLIENT_ID, QueryValues.SANDBOX_CLIENT)
                .queryParam(QueryKeys.RESPONSE_TYPE, QueryValues.RESPONSE_TYPE)
                .queryParam(QueryKeys.REDIRECT_URI, redirectUri)
                .queryParam(QueryKeys.SCOPE, QueryValues.SCOPE)
                .queryParam(QueryKeys.CLIENT_SECRET, QueryValues.SANDBOX)
                .queryParam(QueryKeys.STATE, state)
                .getUrl();
    }

    public OAuth2Token getToken(String code) {
        final String redirectUri = getConfiguration().getRedirectUrl();
        final String clientId = getConfiguration().getClientId();
        final String clientSecret = getConfiguration().getClientSecret();

        // TODO For productions use the real clientId and clientSecret instead of constants
        TokenRequest request =
                new TokenRequest(
                        FormValues.AUTHORIZATION_CODE,
                        code,
                        redirectUri,
                        FormValues.CLIENT_ID,
                        FormValues.CLIENT_SECRET);

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
                        Utils.formatDateTime(endDate, Format.TIMESTAMP, Format.TIMEZONE))
                .queryParam(
                        QueryKeys.DATE_FROM,
                        Utils.formatDateTime(startDate, Format.TIMESTAMP, Format.TIMEZONE))
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
}
