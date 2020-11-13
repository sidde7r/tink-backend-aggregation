package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.entercard;

import java.util.NoSuchElementException;
import java.util.Optional;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.entercard.EnterCardConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.entercard.EnterCardConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.entercard.EnterCardConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.entercard.EnterCardConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.entercard.EnterCardConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.entercard.EnterCardConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.entercard.authenticator.rpc.AuthorizationCodeResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.entercard.authenticator.rpc.ErrorResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.entercard.authenticator.rpc.RefreshTokenRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.entercard.authenticator.rpc.TokenRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.entercard.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.entercard.configuration.EnterCardConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.entercard.fetcher.entities.TransactionKey;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.entercard.fetcher.rpc.CreditCardAccountResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.entercard.fetcher.rpc.CreditCardTransactionsResponse;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public final class EnterCardApiClient {

    private final TinkHttpClient client;
    private final PersistentStorage persistentStorage;
    private EnterCardConfiguration configuration;
    private String redirectUrl;

    public EnterCardApiClient(TinkHttpClient client, PersistentStorage persistentStorage) {
        this.client = client;
        this.persistentStorage = persistentStorage;
    }

    private EnterCardConfiguration getConfiguration() {
        return Optional.ofNullable(configuration)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    private String getRedirectUrl() {
        return Optional.ofNullable(redirectUrl)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    protected void setConfiguration(AgentConfiguration<EnterCardConfiguration> agentConfiguration) {
        this.configuration = agentConfiguration.getProviderSpecificConfiguration();
        this.redirectUrl = agentConfiguration.getRedirectUrl();
    }

    private RequestBuilder createRequest(URL url) {
        return client.request(url)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON);
    }

    private RequestBuilder createRequestInSession(URL url) {
        final OAuth2Token authToken = getTokenFromStorage();
        final String clientId = configuration.getClientId();

        return createRequest(url).header(HeaderKeys.API_KEY, clientId).addBearerToken(authToken);
    }

    private OAuth2Token getTokenFromStorage() {
        return persistentStorage
                .get(StorageKeys.OAUTH_TOKEN, OAuth2Token.class)
                .orElseThrow(
                        () -> new IllegalStateException(SessionError.SESSION_EXPIRED.exception()));
    }

    public CreditCardAccountResponse fetchCreditCardAccounts(String ssn) {
        return createRequestInSession(Urls.ACCOUNTS)
                .queryParam(QueryKeys.SSN, ssn)
                .get(CreditCardAccountResponse.class);
    }

    public TransactionKeyPaginatorResponse fetchCreditCardTransactions(
            CreditCardAccount account, TransactionKey key) {
        return createRequestInSession(EnterCardConstants.Urls.TRANSACTIONS)
                .queryParam(QueryKeys.INCLUDE_CARD_MOVEMENTS, QueryValues.TRUE)
                .queryParam(QueryKeys.ACCOUNT_NUMBER, account.getApiIdentifier())
                .queryParam(QueryKeys.START_AT_ROW_NUMBER, String.valueOf(key.getStartAtRowNum()))
                .queryParam(
                        QueryKeys.STOP_AFTER_ROW_NUMBER, String.valueOf(key.getStopAfterRowNum()))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .get(CreditCardTransactionsResponse.class);
    }

    public OAuth2Token getAuth() {
        final String clientId = getConfiguration().getClientId();

        AuthorizationCodeResponse response =
                client.request(Urls.AUTHORIZATION)
                        .accept(MediaType.APPLICATION_JSON)
                        .queryParam(QueryKeys.RESPONSE_TYPE, QueryValues.RESPONSE_TYPE)
                        .queryParam(QueryKeys.CLIENT_ID, clientId)
                        .get(AuthorizationCodeResponse.class);

        persistentStorage.put(StorageKeys.AUTH_CODE, response.getCode());
        setTokenToStorage(getToken(response.getCode()));

        return getToken(response.getCode());
    }

    public OAuth2Token getToken(String code) {
        final String clientId = getConfiguration().getClientId();
        final String redirectUri = getRedirectUrl();
        final String clientSecret = getConfiguration().getClientSecret();

        TokenRequest request =
                new TokenRequest(
                        QueryValues.SCOPE,
                        code,
                        QueryValues.GRANT_TYPE,
                        clientId,
                        clientSecret,
                        redirectUri);

        return client.request(Urls.TOKEN)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON)
                .post(TokenResponse.class, request.toData())
                .toTinkToken();
    }

    public OAuth2Token refreshToken(String refreshToken) throws SessionException {
        final String clientId = getConfiguration().getClientId();
        final String clientSecret = getConfiguration().getClientSecret();

        try {
            RefreshTokenRequest request =
                    new RefreshTokenRequest(
                            QueryValues.SCOPE,
                            QueryValues.REFRESH_TOKEN,
                            refreshToken,
                            clientId,
                            clientSecret);

            return client.request(Urls.TOKEN)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED)
                    .accept(MediaType.APPLICATION_JSON)
                    .post(TokenResponse.class, request.toData())
                    .toTinkToken();

        } catch (HttpResponseException e) {
            if (e.getResponse().getStatus() == HttpStatus.SC_BAD_REQUEST
                    && e.getResponse().getBody(ErrorResponse.class).isInvalidGrant()) {
                throw SessionError.SESSION_EXPIRED.exception();
            }
            throw e;
        }
    }

    public void setTokenToStorage(OAuth2Token token) {
        persistentStorage.put(StorageKeys.OAUTH_TOKEN, token);
    }

    public OAuth2Token getPersistRefreshToken() {
        return persistentStorage
                .get(StorageKeys.OAUTH_TOKEN, OAuth2Token.class)
                .orElseThrow(
                        () ->
                                new NoSuchElementException(
                                        "Cannot refresh access token, could not fetch persisted token object"));
    }
}
