package se.tink.backend.aggregation.agents.nxgen.be.openbanking.deutschebank;

import java.util.Optional;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.deutschebank.DeutscheBankConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.deutschebank.DeutscheBankConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.deutschebank.DeutscheBankConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.deutschebank.DeutscheBankConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.deutschebank.DeutscheBankConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.deutschebank.DeutscheBankConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.deutschebank.authenticator.rpc.RefreshRequest;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.deutschebank.authenticator.rpc.TokenRequest;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.deutschebank.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.deutschebank.configuration.DeutscheBankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.deutschebank.fetcher.transactionalaccount.rpc.FetchAccountResponse;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.deutschebank.fetcher.transactionalaccount.rpc.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.deutschebank.fetcher.transactionalaccount.rpc.PartnersResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public final class DeutscheBankApiClient {

    private final TinkHttpClient client;
    private final SessionStorage sessionStorage;
    private DeutscheBankConfiguration configuration;

    DeutscheBankApiClient(TinkHttpClient client, SessionStorage sessionStorage) {
        this.client = client;
        this.sessionStorage = sessionStorage;
    }

    public DeutscheBankConfiguration getConfiguration() {
        return Optional.ofNullable(configuration)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    public void setConfiguration(DeutscheBankConfiguration configuration) {
        this.configuration = configuration;
    }

    private RequestBuilder createRequestInSession(String url) {
        return client.request(url).addBearerToken(getTokenFromSession());
    }

    public PartnersResponse fetchPartners() {
        final String baseUrl = getConfiguration().getBaseUrl();

        return createRequestInSession(baseUrl + Urls.PARTNERS).get(PartnersResponse.class);
    }

    public FetchAccountResponse fetchAccounts() {
        final String baseUrl = getConfiguration().getBaseUrl();

        return createRequestInSession(baseUrl + Urls.ACCOUNTS).get(FetchAccountResponse.class);
    }

    public FetchTransactionsResponse fetchTransactions(TransactionalAccount account, int page) {
        final String baseUrl = getConfiguration().getBaseUrl();

        return createRequestInSession(baseUrl + Urls.TRANSACTIONS)
                .queryParam(QueryKeys.IBAN, account.getFromTemporaryStorage(StorageKeys.ACCOUNT_ID))
                .queryParam(QueryKeys.OFFSET, Integer.toString(page))
                .get(FetchTransactionsResponse.class);
    }

    public URL getAuthorizeUrl(String state) {
        final String clientId = getConfiguration().getClientId();

        final String baseUrl = getConfiguration().getBaseUrl();
        final String redirectUri = getConfiguration().getRedirectUri();

        return client.request(baseUrl + Urls.OAUTH)
                .queryParam(QueryKeys.CLIENT_ID, clientId)
                .queryParam(QueryKeys.RESPONSE_TYPE, QueryValues.RESPONSE_TYPE)
                .queryParam(QueryKeys.REDIRECT_URI, redirectUri)
                .queryParam(QueryKeys.STATE, state)
                .getUrl();
    }

    public OAuth2Token refreshToken(String token) throws SessionException {
        final String clientId = getConfiguration().getClientId();
        final String clientSecret = getConfiguration().getClientSecret();

        final String baseUrl = getConfiguration().getBaseUrl();

        try {
            final RefreshRequest request =
                    new RefreshRequest(FormValues.GRANT_TYPE, clientId, clientSecret, token);

            return client.request(baseUrl + Urls.TOKEN)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED)
                    .post(TokenResponse.class, request.toData())
                    .toTinkToken();
        } catch (HttpResponseException e) {
            if (e.getResponse().getStatus() == 401) {
                throw SessionError.SESSION_EXPIRED.exception();
            }
            throw e;
        }
    }

    public OAuth2Token getToken(String code) {
        final String clientId = getConfiguration().getClientId();
        final String clientSecret = getConfiguration().getClientSecret();

        final String baseUrl = getConfiguration().getBaseUrl();
        final String redirectUri = getConfiguration().getRedirectUri();

        final TokenRequest request = new TokenRequest(FormValues.GRANT_TYPE, code, redirectUri);

        return client.request(baseUrl + Urls.TOKEN)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED)
                .addBasicAuth(clientId, clientSecret)
                .post(TokenResponse.class, request.toData())
                .toTinkToken();
    }

    public void setTokenToSession(OAuth2Token token) {
        sessionStorage.put(StorageKeys.TOKEN, token);
    }

    private OAuth2Token getTokenFromSession() {
        return sessionStorage
                .get(StorageKeys.TOKEN, OAuth2Token.class)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_TOKEN));
    }
}
