package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase;

import java.time.LocalDate;
import java.util.Collection;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.SebAccountsAndCardsConstants;
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

    public TinkHttpClient getClient() {
        return this.client;
    }

    public void setConfiguration(SebConfiguration configuration) {
        this.configuration = configuration;
    }

    public SebConfiguration getConfiguration() {
        return configuration;
    }

    public OAuth2Token getToken(String url, TokenRequest request) {
        return client.request(new URL(url))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON)
                .post(TokenResponse.class, request.toData())
                .toTinkToken();
    }

    public OAuth2Token refreshToken(String url, RefreshRequest request) throws SessionException {
        try {
            return client.request(new URL(url))
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

    public RequestBuilder buildAuthorizeUrl() {
        return client.request(new URL(SebAccountsAndCardsConstants.Urls.BASE_AUTH_URL));
    }

    public void setTokenToSession(OAuth2Token token) {
        sessionStorage.put(SebCommonConstants.StorageKeys.TOKEN, token);
    }

    protected OAuth2Token getTokenFromSession() {
        return sessionStorage
                .get(SebCommonConstants.StorageKeys.TOKEN, OAuth2Token.class)
                .orElseThrow(() -> new IllegalStateException("Cannot find token!"));
    }

    protected RequestBuilder createRequestInSession(URL url) {
        return client.request(url)
                .accept(MediaType.APPLICATION_JSON)
                .header(SebCommonConstants.HeaderKeys.X_REQUEST_ID, getRequestId())
                .header(
                        SebCommonConstants.HeaderKeys.PSU_IP_ADDRESS,
                        SebCommonConstants.getPsuIpAddress())
                .addBearerToken(getTokenFromSession());
    }

    protected String getRequestId() {
        return java.util.UUID.randomUUID().toString();
    }

    public abstract FetchCardAccountsTransactions fetchCardTransactions(
            String bankIdentifier, LocalDate fromDate, LocalDate toDate);

    public abstract Collection<CreditCardAccount> fetchCardAccounts();
}
