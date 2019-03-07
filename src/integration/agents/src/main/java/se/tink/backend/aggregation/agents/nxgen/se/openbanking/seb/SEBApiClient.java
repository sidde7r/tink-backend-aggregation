package se.tink.backend.aggregation.agents.nxgen.se.openbanking.seb;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.seb.authenticator.rpc.RefreshRequest;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.seb.authenticator.rpc.TokenRequest;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.seb.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.seb.fetcher.transactionalaccount.rpc.FetchAccountResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.seb.fetcher.transactionalaccount.rpc.FetchTransactionsResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public final class SEBApiClient {
    private final TinkHttpClient client;
    private final SessionStorage sessionStorage;
    private final PersistentStorage persistenceStorage;

    public SEBApiClient(TinkHttpClient client, SessionStorage sessionStorage, PersistentStorage persistenceStorage) {
        this.client = client;
        this.sessionStorage = sessionStorage;
        this.persistenceStorage = persistenceStorage;
    }

    public FetchAccountResponse fetchAccounts() {
        return client.request(new URL(SEBConstants.Urls.BASE_AIS + SEBConstants.Urls.ACCOUNTS))
                .accept(MediaType.APPLICATION_JSON)
                .header(SEBConstants.HeaderKeys.X_REQUEST_ID, getRequestId())
                .addBearerToken(getTokenFromSession())
                .queryParam(SEBConstants.QueryKeys.WITH_BALANCE, SEBConstants.QueryValues.WITH_BALANCE)
                .get(FetchAccountResponse.class);
    }

    public FetchTransactionsResponse fetchTransactions(TransactionalAccount account, int page) {
        return client.request(new URL(SEBConstants.Urls.BASE_AIS + SEBConstants.Urls.TRANSACTIONS)
                .parameter(SEBConstants.IdTags.ACCOUNT_ID,
                        account.getFromTemporaryStorage(SEBConstants.STORAGE.ACCOUNT_ID)))
                .accept(MediaType.APPLICATION_JSON)
                .header(SEBConstants.HeaderKeys.X_REQUEST_ID, getRequestId())
                .addBearerToken(getTokenFromSession())
                .queryParam(SEBConstants.QueryKeys.TRANSACTION_SEQUENCE_NUMBER, Integer.toString(page))
                .queryParam(SEBConstants.QueryKeys.BOOKING_STATUS, SEBConstants.QueryValues.BOOKED_TRANSACTIONS)
                .get(FetchTransactionsResponse.class);
    }

    public URL getAuthorizeUrl(String state) {
        return createRequestInSession(new URL(SEBConstants.Urls.BASE_AUTH + SEBConstants.Urls.OAUTH))
                .queryParam(SEBConstants.QueryKeys.CLIENT_ID, persistenceStorage.get(SEBConstants.STORAGE.CLIENT_ID))
                .queryParam(SEBConstants.QueryKeys.RESPONSE_TYPE, SEBConstants.QueryValues.RESPONSE_TYPE_TOKEN)
                .queryParam(SEBConstants.QueryKeys.SCOPE, SEBConstants.QueryValues.SCOPE)
                .queryParam(SEBConstants.QueryKeys.REDIRECT_URI,
                        persistenceStorage.get(SEBConstants.STORAGE.REDIRECT_URI))
                .queryParam(SEBConstants.QueryKeys.STATE, state)
                .getUrl();
    }

    public OAuth2Token getToken(String code) {
        TokenRequest request = new TokenRequest(persistenceStorage.get(SEBConstants.STORAGE.CLIENT_ID),
                persistenceStorage.get(SEBConstants.STORAGE.CLIENT_SECRET),
                persistenceStorage.get(SEBConstants.STORAGE.REDIRECT_URI), code,
                SEBConstants.QueryValues.GRAND_TYPE, SEBConstants.QueryValues.SCOPE);

        return client.request(new URL(SEBConstants.Urls.BASE_AUTH + SEBConstants.Urls.TOKEN))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON)
                .post(TokenResponse.class, request.toData()).toTinkToken();
    }

    public OAuth2Token refreshToken(String refreshToken) throws SessionException {
        try {
            RefreshRequest request = new RefreshRequest(refreshToken,
                    persistenceStorage.get(SEBConstants.STORAGE.CLIENT_ID),
                    persistenceStorage.get(SEBConstants.STORAGE.CLIENT_SECRET),
                    persistenceStorage.get(SEBConstants.STORAGE.REDIRECT_URI));

            return client.request(new URL(SEBConstants.Urls.BASE_AUTH + SEBConstants.Urls.TOKEN))
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED)
                    .accept(MediaType.APPLICATION_JSON)
                    .post(TokenResponse.class, request.toData()).toTinkToken();

        } catch (HttpResponseException e) {
            if (e.getResponse().getStatus() == 401) {
                throw SessionError.SESSION_EXPIRED.exception();
            }
            throw e;
        }
    }

    public void setTokenToSession(OAuth2Token token) {
        sessionStorage.put(SEBConstants.STORAGE.TOKEN, token);
    }

    private RequestBuilder createRequest(URL url) {
        return client.request(url)
                .accept(MediaType.TEXT_HTML);
    }

    private RequestBuilder createRequestInSession(URL url) {
        return createRequest(url);
    }

    private OAuth2Token getTokenFromSession() {
        return sessionStorage.get(SEBConstants.STORAGE.TOKEN, OAuth2Token.class)
                .orElseThrow(() -> new IllegalStateException("Cannot find token!"));
    }

    private String getRequestId() {
        return java.util.UUID.randomUUID().toString();
    }
}
