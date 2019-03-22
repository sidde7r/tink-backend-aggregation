package se.tink.backend.aggregation.agents.nxgen.be.openbanking.deutschebank;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.deutschebank.authenticator.rpc.RefreshRequest;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.deutschebank.authenticator.rpc.TokenRequest;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.deutschebank.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.deutschebank.fetcher.transactionalaccount.rpc.FetchAccountResponse;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.deutschebank.fetcher.transactionalaccount.rpc.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.deutschebank.fetcher.transactionalaccount.rpc.PartnersResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public final class DeutscheBankApiClient {

    private final TinkHttpClient client;
    private final SessionStorage sessionStorage;
    private final PersistentStorage persistentStorage;

    public DeutscheBankApiClient(
        TinkHttpClient client, SessionStorage sessionStorage, PersistentStorage persistentStorage) {
        this.client = client;
        this.sessionStorage = sessionStorage;
        this.persistentStorage = persistentStorage;
    }

    public PartnersResponse fetchPartners() {
        return client
            .request(
                new URL(
                    persistentStorage.get(DeutscheBankConstants.StorageKeys.BASE_URL)
                        + DeutscheBankConstants.Urls.PARTNERS))
            .addBearerToken(getTokenFromSession())
            .get(PartnersResponse.class);
    }

    public FetchAccountResponse fetchAccounts() {
        return client
            .request(
                new URL(
                    persistentStorage.get(DeutscheBankConstants.StorageKeys.BASE_URL)
                        + DeutscheBankConstants.Urls.ACCOUNTS))
            .addBearerToken(getTokenFromSession())
            .get(FetchAccountResponse.class);
    }

    public FetchTransactionsResponse fetchTransactions(TransactionalAccount account, int page) {
        return client
            .request(
                new URL(
                    persistentStorage.get(DeutscheBankConstants.StorageKeys.BASE_URL)
                        + DeutscheBankConstants.Urls.TRANSACTIONS))
            .addBearerToken(getTokenFromSession())
            .queryParam(
                DeutscheBankConstants.QueryKeys.IBAN,
                account.getFromTemporaryStorage(DeutscheBankConstants.StorageKeys.ACCOUNT_ID))
            .queryParam(DeutscheBankConstants.QueryKeys.OFFSET, Integer.toString(page))
            .get(FetchTransactionsResponse.class);
    }

    public URL getAuthorizeUrl(String state) {
        return client
            .request(
                new URL(
                    persistentStorage.get(DeutscheBankConstants.StorageKeys.BASE_URL)
                        + DeutscheBankConstants.Urls.OAUTH))
            .queryParam(
                DeutscheBankConstants.QueryKeys.CLIENT_ID,
                persistentStorage.get(DeutscheBankConstants.StorageKeys.CLIENT_ID))
            .queryParam(
                DeutscheBankConstants.QueryKeys.RESPONSE_TYPE,
                DeutscheBankConstants.QueryValues.RESPONSE_TYPE)
            .queryParam(
                DeutscheBankConstants.QueryKeys.REDIRECT_URI,
                persistentStorage.get(DeutscheBankConstants.StorageKeys.REDIRECT_URI))
            .queryParam(DeutscheBankConstants.QueryKeys.STATE, state)
            .getUrl();
    }

    public OAuth2Token refreshToken(String token) throws SessionException {
        try {
            RefreshRequest request =
                new RefreshRequest(
                    DeutscheBankConstants.FormValues.GRANT_TYPE,
                    persistentStorage.get(DeutscheBankConstants.StorageKeys.CLIENT_ID),
                    persistentStorage.get(DeutscheBankConstants.StorageKeys.CLIENT_SECRET),
                    token);

            return client
                .request(
                    new URL(
                        persistentStorage.get(DeutscheBankConstants.StorageKeys.BASE_URL)
                            + DeutscheBankConstants.Urls.TOKEN))
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
        TokenRequest request =
            new TokenRequest(
                DeutscheBankConstants.FormValues.GRANT_TYPE,
                code,
                persistentStorage.get(DeutscheBankConstants.StorageKeys.REDIRECT_URI));

        return client
            .request(
                new URL(
                    persistentStorage.get(DeutscheBankConstants.StorageKeys.BASE_URL)
                        + DeutscheBankConstants.Urls.TOKEN))
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED)
            .addBasicAuth(
                persistentStorage.get(DeutscheBankConstants.StorageKeys.CLIENT_ID),
                persistentStorage.get(DeutscheBankConstants.StorageKeys.CLIENT_SECRET))
            .post(TokenResponse.class, request.toData())
            .toTinkToken();
    }

    public void setTokenToSession(OAuth2Token token) {
        sessionStorage.put(DeutscheBankConstants.StorageKeys.TOKEN, token);
    }

    private OAuth2Token getTokenFromSession() {
        return sessionStorage
            .get(DeutscheBankConstants.StorageKeys.TOKEN, OAuth2Token.class)
            .orElseThrow(() -> new IllegalStateException("Cannot find token!"));
    }
}
