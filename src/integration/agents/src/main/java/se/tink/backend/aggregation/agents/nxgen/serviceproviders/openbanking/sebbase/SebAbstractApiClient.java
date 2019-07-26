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

    public void setTokenToSession(OAuth2Token token) {
        sessionStorage.put(SebCommonConstants.StorageKeys.TOKEN, token);
    }

    public abstract RequestBuilder getAuthorizeUrl();

    public abstract OAuth2Token getToken(TokenRequest request);

    public SebConfiguration getConfiguration() {
        return configuration;
    }

    public TinkHttpClient getClient() {
        return this.client;
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
}
