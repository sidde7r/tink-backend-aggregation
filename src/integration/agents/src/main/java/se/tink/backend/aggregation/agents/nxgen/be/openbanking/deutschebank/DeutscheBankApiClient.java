package se.tink.backend.aggregation.agents.nxgen.be.openbanking.deutschebank;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.deutschebank.DeutscheBankConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.deutschebank.DeutscheBankConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.deutschebank.DeutscheBankConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.deutschebank.DeutscheBankConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.deutschebank.DeutscheBankConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.deutschebank.authenticator.rpc.RefreshRequest;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.deutschebank.authenticator.rpc.TokenRequest;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.deutschebank.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.deutschebank.fetcher.transactionalaccount.rpc.FetchAccountResponse;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.deutschebank.fetcher.transactionalaccount.rpc.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.deutschebank.fetcher.transactionalaccount.rpc.PartnersResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public final class DeutscheBankApiClient {

    private final TinkHttpClient client;
    private final SessionStorage sessionStorage;
    private final PersistentStorage persistentStorage;

    DeutscheBankApiClient(
            TinkHttpClient client,
            SessionStorage sessionStorage,
            PersistentStorage persistentStorage) {
        this.client = client;
        this.sessionStorage = sessionStorage;
        this.persistentStorage = persistentStorage;
    }

    private RequestBuilder createRequestInSession(String url) {
        return client.request(new URL(url)).addBearerToken(getTokenFromSession());
    }

    public PartnersResponse fetchPartners() {
        final String url = persistentStorage.get(StorageKeys.BASE_URL) + Urls.PARTNERS;

        return createRequestInSession(url).get(PartnersResponse.class);
    }

    public FetchAccountResponse fetchAccounts() {
        final String url = persistentStorage.get(StorageKeys.BASE_URL) + Urls.ACCOUNTS;

        return createRequestInSession(url).get(FetchAccountResponse.class);
    }

    public FetchTransactionsResponse fetchTransactions(TransactionalAccount account, int page) {
        final String url = persistentStorage.get(StorageKeys.BASE_URL) + Urls.TRANSACTIONS;

        return createRequestInSession(url)
                .queryParam(QueryKeys.IBAN, account.getFromTemporaryStorage(StorageKeys.ACCOUNT_ID))
                .queryParam(QueryKeys.OFFSET, Integer.toString(page))
                .get(FetchTransactionsResponse.class);
    }

    public URL getAuthorizeUrl(String state) {
        final String url = persistentStorage.get(StorageKeys.BASE_URL) + Urls.OAUTH;

        return createRequestInSession(url)
                .queryParam(QueryKeys.CLIENT_ID, persistentStorage.get(StorageKeys.CLIENT_ID))
                .queryParam(QueryKeys.RESPONSE_TYPE, QueryValues.RESPONSE_TYPE)
                .queryParam(QueryKeys.REDIRECT_URI, persistentStorage.get(StorageKeys.REDIRECT_URI))
                .queryParam(QueryKeys.STATE, state)
                .getUrl();
    }

    public OAuth2Token refreshToken(String token) throws SessionException {
        try {
            final RefreshRequest request =
                    new RefreshRequest(
                            FormValues.GRANT_TYPE,
                            persistentStorage.get(StorageKeys.CLIENT_ID),
                            persistentStorage.get(StorageKeys.CLIENT_SECRET),
                            token);

            return client.request(new URL(persistentStorage.get(StorageKeys.BASE_URL) + Urls.TOKEN))
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
        final TokenRequest request =
                new TokenRequest(
                        FormValues.GRANT_TYPE,
                        code,
                        persistentStorage.get(StorageKeys.REDIRECT_URI));

        return client.request(new URL(persistentStorage.get(StorageKeys.BASE_URL) + Urls.TOKEN))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED)
                .addBasicAuth(
                        persistentStorage.get(StorageKeys.CLIENT_ID),
                        persistentStorage.get(StorageKeys.CLIENT_SECRET))
                .post(TokenResponse.class, request.toData())
                .toTinkToken();
    }

    public void setTokenToSession(OAuth2Token token) {
        sessionStorage.put(StorageKeys.TOKEN, token);
    }

    private OAuth2Token getTokenFromSession() {
        return sessionStorage
                .get(StorageKeys.TOKEN, OAuth2Token.class)
                .orElseThrow(() -> new IllegalStateException("Cannot find token!"));
    }
}
