package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase;

import javax.ws.rs.core.MediaType;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.authenticator.rpc.GetTokenForm;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.authenticator.rpc.GetTokenResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.authenticator.rpc.RefreshTokenForm;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.fetcher.transactionalaccount.rpc.GetAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.fetcher.transactionalaccount.rpc.GetTransactionsResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class NordeaBaseApiClient {
    protected final TinkHttpClient client;
    protected final SessionStorage sessionStorage;
    protected final PersistentStorage persistentStorage;

    public NordeaBaseApiClient(
            TinkHttpClient client,
            SessionStorage sessionStorage,
            PersistentStorage persistentStorage) {
        this.client = client;
        this.sessionStorage = sessionStorage;
        this.persistentStorage = persistentStorage;
    }

    protected RequestBuilder createRequest(URL url) {
        return client.request(url)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .header(
                        NordeaBaseConstants.QueryKeys.X_CLIENT_ID,
                        persistentStorage.get(NordeaBaseConstants.StorageKeys.CLIENT_ID))
                .header(
                        NordeaBaseConstants.QueryKeys.X_CLIENT_SECRET,
                        persistentStorage.get(NordeaBaseConstants.StorageKeys.CLIENT_SECRET));
    }

    private RequestBuilder createRequestInSession(URL url) {
        OAuth2Token token = getTokenFromSession();
        return createRequest(url)
                .header(
                        NordeaBaseConstants.HeaderKeys.AUTHORIZATION,
                        token.getTokenType() + " " + token.getAccessToken());
    }

    private RequestBuilder createTokenRequest() {
        return createRequest(new URL(NordeaBaseConstants.Urls.GET_TOKEN));
    }

    public URL getAuthorizeUrl(String state, String country) {
        return client.request(
                        new URL(NordeaBaseConstants.Urls.AUTHORIZE)
                                .queryParam(
                                        NordeaBaseConstants.QueryKeys.CLIENT_ID,
                                        persistentStorage.get(
                                                NordeaBaseConstants.StorageKeys.CLIENT_ID))
                                .queryParam(NordeaBaseConstants.QueryKeys.STATE, state)
                                .queryParam(
                                        NordeaBaseConstants.QueryKeys.DURATION,
                                        NordeaBaseConstants.QueryValues.DURATION)
                                .queryParam(NordeaBaseConstants.QueryKeys.COUNTRY, country)
                                .queryParam(
                                        NordeaBaseConstants.QueryKeys.SCOPE,
                                        NordeaBaseConstants.QueryValues.SCOPE)
                                .queryParam(
                                        NordeaBaseConstants.QueryKeys.REDIRECT_URI,
                                        persistentStorage.get(
                                                NordeaBaseConstants.StorageKeys.REDIRECT_URI)))
                .getUrl();
    }

    public OAuth2Token getToken(GetTokenForm form) {
        return createTokenRequest()
                .body(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                .post(GetTokenResponse.class)
                .toTinkToken();
    }

    public OAuth2Token refreshToken(RefreshTokenForm form) {
        return createTokenRequest()
                .body(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                .post(GetTokenResponse.class)
                .toTinkToken();
    }

    public GetAccountsResponse getAccounts() {
        return createRequestInSession(new URL(NordeaBaseConstants.Urls.GET_ACCOUNTS))
                .get(GetAccountsResponse.class);
    }

    public GetTransactionsResponse getTransactions(TransactionalAccount account, String key) {
        URL url =
                key == null
                        ? new URL(NordeaBaseConstants.Urls.GET_TRANSACTIONS)
                                .parameter(
                                        NordeaBaseConstants.IdTags.ACCOUNT_ID,
                                        account.getApiIdentifier())
                        : new URL(NordeaBaseConstants.Urls.BASE_URL + key);
        return createRequestInSession(url).get(GetTransactionsResponse.class);
    }

    public void setTokenToSession(OAuth2Token accessToken) {
        sessionStorage.put(NordeaBaseConstants.StorageKeys.ACCESS_TOKEN, accessToken);
    }

    private OAuth2Token getTokenFromSession() {
        return sessionStorage
                .get(NordeaBaseConstants.StorageKeys.ACCESS_TOKEN, OAuth2Token.class)
                .orElseThrow(() -> new IllegalStateException("Cannot find token!"));
    }
}
