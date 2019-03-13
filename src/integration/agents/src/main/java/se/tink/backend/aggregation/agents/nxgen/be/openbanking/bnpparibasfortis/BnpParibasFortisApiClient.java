package se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis;

import java.util.UUID;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.authenticator.rpc.RefreshTokenRequest;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.authenticator.rpc.TokenRequest;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.fetcher.transactionalaccount.entity.account.Account;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.fetcher.transactionalaccount.entity.account.Links;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.fetcher.transactionalaccount.entity.balance.GetBalancesResponse;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.fetcher.transactionalaccount.rpc.GetTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.fetcher.transactionalaccount.rpc.GetAccountsResponse;
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

    public BnpParibasFortisApiClient(TinkHttpClient client, SessionStorage sessionStorage,
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
            .get(BnpParibasFortisConstants.StorageKeys.OAUTH_TOKEN, OAuth2Token.class)
            .orElseThrow(() -> new IllegalStateException("Cannot find token!"));
    }

    private RequestBuilder createAuthenticatedRequest(URL url) {
        return createRequest(url)
            .addBearerToken(getTokenFromSession())
            .header(
                BnpParibasFortisConstants.HeaderKeys.OPENBANK_STET_VERSION,
                persistentStorage.get(BnpParibasFortisConstants.StorageKeys.OPENBANK_STET_VERSION)
            )
            .header(
                BnpParibasFortisConstants.HeaderKeys.ORGANIZATION_ID,
                persistentStorage.get(BnpParibasFortisConstants.StorageKeys.ORGANIZATION_ID)
            )
            .header(
                BnpParibasFortisConstants.HeaderKeys.REQUEST_ID,
                UUID.randomUUID().toString()
            )
            .header(
                BnpParibasFortisConstants.HeaderKeys.SIGNATURE,
                UUID.randomUUID().toString()
            )
            .accept("application/hal+json");
    }

    public URL getAuthorizeUrl(String state) {
        String baseUrl = persistentStorage.get(BnpParibasFortisConstants.StorageKeys.AUTH_BASE_URL);
        String oauthUrl = baseUrl + BnpParibasFortisConstants.Urls.OAUTH;

        return createRequest(new URL(oauthUrl))
            .queryParam(
                BnpParibasFortisConstants.QueryKeys.RESPONSE_TYPE,
                BnpParibasFortisConstants.QueryValues.RESPONSE_TYPE)
            .queryParam(
                BnpParibasFortisConstants.QueryKeys.CLIENT_ID,
                persistentStorage.get(BnpParibasFortisConstants.StorageKeys.CLIENT_ID))
            .queryParam(
                BnpParibasFortisConstants.QueryKeys.REDIRECT_URI,
                persistentStorage.get(BnpParibasFortisConstants.StorageKeys.REDIRECT_URI))
            .queryParam(
                BnpParibasFortisConstants.QueryKeys.SCOPE,
                BnpParibasFortisConstants.QueryValues.SCOPE)
            .queryParam(
                BnpParibasFortisConstants.QueryKeys.STATE,
                state)
            .getUrl();
    }

    public OAuth2Token getToken(String code) {
        String baseUrl = persistentStorage.get(BnpParibasFortisConstants.StorageKeys.AUTH_BASE_URL);
        String tokenUrl = baseUrl + BnpParibasFortisConstants.Urls.TOKEN;

        TokenResponse response = createRequest(new URL(tokenUrl))
            .header(
                BnpParibasFortisConstants.HeaderKeys.ORGANIZATION_ID,
                persistentStorage.get(BnpParibasFortisConstants.StorageKeys.ORGANIZATION_ID))
            .header(
                BnpParibasFortisConstants.HeaderKeys.OPENBANK_STET_VERSION,
                persistentStorage.get(BnpParibasFortisConstants.StorageKeys.OPENBANK_STET_VERSION))
            .body(TokenRequest
                    .builder()
                    .clientId(persistentStorage.get(BnpParibasFortisConstants.StorageKeys.CLIENT_ID))
                    .clientSecret(
                        persistentStorage.get(BnpParibasFortisConstants.StorageKeys.CLIENT_SECRET))
                    .code(code)
                    .grantType(BnpParibasFortisConstants.FormValues.GRANT_TYPE)
                    .redirectUri(
                        persistentStorage.get(BnpParibasFortisConstants.StorageKeys.REDIRECT_URI))
                    .scope(BnpParibasFortisConstants.FormValues.SCOPE)
                    .build(),
                MediaType.APPLICATION_JSON
            )
            .accept(MediaType.APPLICATION_JSON)
            .post(TokenResponse.class);

        return OAuth2Token.create(
            response.getTokenType(),
            response.getToken(),
            response.getRefresh(),
            response.getExpiresIn()
        );
    }

    public OAuth2Token refreshToken(String refreshToken) {
        String baseUrl = persistentStorage.get(BnpParibasFortisConstants.StorageKeys.AUTH_BASE_URL);
        String tokenUrl = baseUrl + BnpParibasFortisConstants.Urls.TOKEN;

        TokenResponse response = createRequest(new URL(tokenUrl))
            .header(
                BnpParibasFortisConstants.HeaderKeys.ORGANIZATION_ID,
                persistentStorage.get(BnpParibasFortisConstants.StorageKeys.ORGANIZATION_ID))
            .header(
                BnpParibasFortisConstants.HeaderKeys.OPENBANK_STET_VERSION,
                persistentStorage.get(BnpParibasFortisConstants.StorageKeys.OPENBANK_STET_VERSION))
            .body(RefreshTokenRequest
                    .builder()
                    .clientId(persistentStorage.get(BnpParibasFortisConstants.StorageKeys.CLIENT_ID))
                    .clientSecret(
                        persistentStorage.get(BnpParibasFortisConstants.StorageKeys.CLIENT_SECRET))
                    .refreshToken(refreshToken)
                    .grantType(BnpParibasFortisConstants.FormValues.GRANT_TYPE)
                    .redirectUri(
                        persistentStorage.get(BnpParibasFortisConstants.StorageKeys.REDIRECT_URI))
                    .scope(BnpParibasFortisConstants.FormValues.SCOPE)
                    .build(),
                MediaType.APPLICATION_JSON
            )
            .accept(MediaType.APPLICATION_JSON)
            .post(TokenResponse.class);

        return OAuth2Token.create(
            response.getTokenType(),
            response.getToken(),
            response.getRefresh(),
            response.getExpiresIn()
        );
    }

    public GetAccountsResponse getAccounts() {
        String baseUrl = persistentStorage.get(BnpParibasFortisConstants.StorageKeys.API_BASE_URL);
        String accountsUrl = baseUrl + BnpParibasFortisConstants.Urls.ACCOUNTS;

        return createAuthenticatedRequest(new URL(accountsUrl))
            .get(GetAccountsResponse.class);
    }

    public GetBalancesResponse getBalanceForAccount(Account account) {
        String baseUrl = persistentStorage.get(BnpParibasFortisConstants.StorageKeys.API_BASE_URL);
        String balancesUrl = baseUrl + account.getLinks().getBalances().getHref();

        return createAuthenticatedRequest(new URL(balancesUrl))
            .get(GetBalancesResponse.class);
    }

    public GetTransactionsResponse getTransactionsForAccount(TransactionalAccount account) {
        String baseUrl = persistentStorage.get(BnpParibasFortisConstants.StorageKeys.API_BASE_URL);
        String transactionsUrl = account
            .getFromTemporaryStorage(BnpParibasFortisConstants.StorageKeys.ACCOUNT_LINKS,
                Links.class)
            .map(links -> baseUrl + links.getTransactions().getHref())
            .orElseThrow(IllegalStateException::new);

        return createAuthenticatedRequest(new URL(transactionsUrl))
            .get(GetTransactionsResponse.class);
    }
}
