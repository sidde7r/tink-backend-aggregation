package se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis;

import java.util.UUID;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.BnpParibasFortisConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.BnpParibasFortisConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.BnpParibasFortisConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.BnpParibasFortisConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.BnpParibasFortisConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.BnpParibasFortisConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.authenticator.rpc.RefreshTokenRequest;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.authenticator.rpc.TokenRequest;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.fetcher.transactionalaccount.entity.account.Account;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.fetcher.transactionalaccount.entity.account.Links;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.fetcher.transactionalaccount.entity.balance.GetBalancesResponse;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.fetcher.transactionalaccount.rpc.GetAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.fetcher.transactionalaccount.rpc.GetTransactionsResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public final class BnpParibasFortisApiClient {
    private final TinkHttpClient client;
    private final SessionStorage sessionStorage;
    private final PersistentStorage persistentStorage;

    public BnpParibasFortisApiClient(
            TinkHttpClient client,
            SessionStorage sessionStorage,
            PersistentStorage persistentStorage) {
        this.client = client;
        this.sessionStorage = sessionStorage;
        this.persistentStorage = persistentStorage;
    }

    private RequestBuilder createRequest(URL url) {
        return client.request(url)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON);
    }

    private OAuth2Token getTokenFromSession() {
        return sessionStorage
                .get(StorageKeys.OAUTH_TOKEN, OAuth2Token.class)
                .orElseThrow(() -> new IllegalStateException("Cannot find token!"));
    }

    private RequestBuilder createAuthenticatedRequest(URL url) {
        return createRequest(url)
                .addBearerToken(getTokenFromSession())
                .header(
                        HeaderKeys.OPENBANK_STET_VERSION,
                        persistentStorage.get(StorageKeys.OPENBANK_STET_VERSION))
                .header(
                        HeaderKeys.ORGANIZATION_ID,
                        persistentStorage.get(StorageKeys.ORGANIZATION_ID))
                .header(HeaderKeys.REQUEST_ID, UUID.randomUUID().toString())
                .header(HeaderKeys.SIGNATURE, UUID.randomUUID().toString())
                .accept(HeaderKeys.APPLICATION_HAL_JSON);
    }

    public URL getAuthorizeUrl(String state) {
        final String baseUrl = persistentStorage.get(StorageKeys.AUTH_BASE_URL);
        final String oauthUrl = baseUrl + Urls.OAUTH;

        return createRequest(new URL(oauthUrl))
                .queryParam(QueryKeys.RESPONSE_TYPE, QueryValues.RESPONSE_TYPE)
                .queryParam(QueryKeys.CLIENT_ID, persistentStorage.get(StorageKeys.CLIENT_ID))
                .queryParam(QueryKeys.REDIRECT_URI, persistentStorage.get(StorageKeys.REDIRECT_URI))
                .queryParam(QueryKeys.SCOPE, QueryValues.SCOPE)
                .queryParam(QueryKeys.STATE, state)
                .getUrl();
    }

    public OAuth2Token getToken(String code) {
        final String baseUrl = persistentStorage.get(StorageKeys.AUTH_BASE_URL);
        final String tokenUrl = baseUrl + Urls.TOKEN;

        final TokenResponse response =
                createRequest(new URL(tokenUrl))
                        .header(
                                HeaderKeys.ORGANIZATION_ID,
                                persistentStorage.get(StorageKeys.ORGANIZATION_ID))
                        .header(
                                HeaderKeys.OPENBANK_STET_VERSION,
                                persistentStorage.get(StorageKeys.OPENBANK_STET_VERSION))
                        .body(
                                TokenRequest.builder()
                                        .clientId(persistentStorage.get(StorageKeys.CLIENT_ID))
                                        .clientSecret(
                                                persistentStorage.get(StorageKeys.CLIENT_SECRET))
                                        .code(code)
                                        .grantType(FormValues.GRANT_TYPE)
                                        .redirectUri(
                                                persistentStorage.get(StorageKeys.REDIRECT_URI))
                                        .scope(FormValues.SCOPE)
                                        .build(),
                                MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .post(TokenResponse.class);

        return OAuth2Token.create(
                response.getTokenType(),
                response.getToken(),
                response.getRefresh(),
                response.getExpiresIn());
    }

    public OAuth2Token refreshToken(String refreshToken) {
        final String baseUrl = persistentStorage.get(StorageKeys.AUTH_BASE_URL);
        final String tokenUrl = baseUrl + Urls.TOKEN;

        final TokenResponse response =
                createRequest(new URL(tokenUrl))
                        .header(
                                HeaderKeys.ORGANIZATION_ID,
                                persistentStorage.get(StorageKeys.ORGANIZATION_ID))
                        .header(
                                HeaderKeys.OPENBANK_STET_VERSION,
                                persistentStorage.get(StorageKeys.OPENBANK_STET_VERSION))
                        .body(
                                RefreshTokenRequest.builder()
                                        .clientId(persistentStorage.get(StorageKeys.CLIENT_ID))
                                        .clientSecret(
                                                persistentStorage.get(StorageKeys.CLIENT_SECRET))
                                        .refreshToken(refreshToken)
                                        .grantType(FormValues.GRANT_TYPE)
                                        .redirectUri(
                                                persistentStorage.get(StorageKeys.REDIRECT_URI))
                                        .scope(FormValues.SCOPE)
                                        .build(),
                                MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .post(TokenResponse.class);

        return OAuth2Token.create(
                response.getTokenType(),
                response.getToken(),
                response.getRefresh(),
                response.getExpiresIn());
    }

    public GetAccountsResponse getAccounts() {
        final String baseUrl = persistentStorage.get(StorageKeys.API_BASE_URL);
        final String accountsUrl = baseUrl + Urls.ACCOUNTS;

        return createAuthenticatedRequest(new URL(accountsUrl)).get(GetAccountsResponse.class);
    }

    public GetBalancesResponse getBalanceForAccount(Account account) {
        final String baseUrl = persistentStorage.get(StorageKeys.API_BASE_URL);
        final String balancesUrl = baseUrl + account.getLinks().getBalances().getHref();

        return createAuthenticatedRequest(new URL(balancesUrl)).get(GetBalancesResponse.class);
    }

    public GetTransactionsResponse getTransactionsForAccount(TransactionalAccount account) {
        final String baseUrl = persistentStorage.get(StorageKeys.API_BASE_URL);
        final String transactionsUrl =
                account.getFromTemporaryStorage(StorageKeys.ACCOUNT_LINKS, Links.class)
                        .map(links -> baseUrl + links.getTransactions().getHref())
                        .orElseThrow(IllegalStateException::new);

        return createAuthenticatedRequest(new URL(transactionsUrl))
                .get(GetTransactionsResponse.class);
    }
}
