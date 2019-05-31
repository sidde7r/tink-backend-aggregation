package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase;

import java.time.LocalDate;
import java.util.Collection;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.authenticator.rpc.RefreshRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.authenticator.rpc.TokenRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.configuration.SebConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.fetcher.cardaccounts.rpc.FetchCardAccountsTransactions;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public abstract class SebAbstractApiClient {

    protected final TinkHttpClient client;
    protected final SessionStorage sessionStorage;
    protected SebConfiguration configuration;

    public SebAbstractApiClient(TinkHttpClient client, SessionStorage sessionStorage) {
        this.client = client;
        this.sessionStorage = sessionStorage;
    }

    public void setConfiguration(SebConfiguration configuration) {
        this.configuration = configuration;
    }

    public SebConfiguration getConfiguration() {
        return configuration;
    }

    public OAuth2Token getToken(String code) {
        TokenRequest request =
                new TokenRequest(
                        configuration.getClientId(),
                        configuration.getClientSecret(),
                        configuration.getRedirectUrl(),
                        code,
                        SebCommonConstants.QueryValues.GRANT_TYPE,
                        SebCommonConstants.QueryValues.SCOPE);

        OAuth2Token token =
                client.request(new URL(configuration.getBaseUrl() + SebCommonConstants.Urls.TOKEN))
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED)
                        .accept(MediaType.APPLICATION_JSON)
                        .post(TokenResponse.class, request.toData())
                        .toTinkToken();

        return token;
    }

    public URL getAuthorizeUrl(String state) {
        return createRequestInSession(
                        new URL(configuration.getBaseUrl() + SebCommonConstants.Urls.OAUTH))
                .queryParam(SebCommonConstants.QueryKeys.CLIENT_ID, configuration.getClientId())
                .queryParam(
                        SebCommonConstants.QueryKeys.RESPONSE_TYPE,
                        SebCommonConstants.QueryValues.RESPONSE_TYPE_TOKEN)
                .queryParam(
                        SebCommonConstants.QueryKeys.SCOPE, SebCommonConstants.QueryValues.SCOPE)
                .queryParam(
                        SebCommonConstants.QueryKeys.REDIRECT_URI, configuration.getRedirectUrl())
                .queryParam(SebCommonConstants.QueryKeys.STATE, state)
                .getUrl();
    }

    public OAuth2Token refreshToken(String refreshToken) throws SessionException {
        try {
            RefreshRequest request =
                    new RefreshRequest(
                            refreshToken,
                            configuration.getClientId(),
                            configuration.getClientSecret(),
                            configuration.getRedirectUrl());

            return client.request(
                            new URL(configuration.getBaseUrl() + SebCommonConstants.Urls.TOKEN))
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED)
                    .accept(MediaType.APPLICATION_JSON)
                    .post(TokenResponse.class, request.toData())
                    .toTinkToken();

        } catch (HttpResponseException e) {
            if (e.getResponse().getStatus() == 401) {
                throw SessionError.SESSION_EXPIRED.exception();
            }
            throw e;
        }
    }

    public void setTokenToSession(OAuth2Token token) {
        sessionStorage.put(SebCommonConstants.StorageKeys.TOKEN, token);
    }

    protected RequestBuilder createRequest(URL url) {
        return client.request(url).accept(MediaType.TEXT_HTML);
    }

    protected RequestBuilder createRequestInSession(URL url) {
        return createRequest(url);
    }

    protected OAuth2Token getTokenFromSession() {
        return sessionStorage
                .get(SebCommonConstants.StorageKeys.TOKEN, OAuth2Token.class)
                .orElseThrow(() -> new IllegalStateException("Cannot find token!"));
    }

    protected String getRequestId() {
        return java.util.UUID.randomUUID().toString();
    }

    public abstract FetchCardAccountsTransactions fetchCardTransactions(
            String bankIdentifier, LocalDate fromDate, LocalDate toDate);

    public abstract Collection<CreditCardAccount> fetchCardAccounts();
}
