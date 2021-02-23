package se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.http;

import se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.BnpParibasFortisConstants.Endpoints;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.BnpParibasFortisConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.BnpParibasFortisConstants.HeaderValues;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.BnpParibasFortisConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.BnpParibasFortisConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.BnpParibasFortisConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.BnpParibasFortisConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.authenticator.rpc.RefreshTokenRequest;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.authenticator.rpc.TokenRequest;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.configuration.BnpParibasFortisConfiguration;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.fetcher.transactionalaccount.entity.account.Account;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.fetcher.transactionalaccount.entity.account.Links;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.fetcher.transactionalaccount.entity.balance.GetBalancesResponse;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.fetcher.transactionalaccount.rpc.GetAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.fetcher.transactionalaccount.rpc.GetTransactionsResponse;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

import javax.ws.rs.core.MediaType;

public final class BnpParibasFortisApiClient {

    private final TinkHttpClient client;
    private final SessionStorage sessionStorage;
    private BnpParibasFortisConfiguration configuration;
    private final String redirectUrl;

    public BnpParibasFortisApiClient(
            TinkHttpClient client,
            SessionStorage sessionStorage,
            AgentConfiguration<BnpParibasFortisConfiguration> agentConfiguration) {
        this.client = client;
        this.sessionStorage = sessionStorage;
        this.configuration = agentConfiguration.getProviderSpecificConfiguration();
        this.redirectUrl = agentConfiguration.getRedirectUrl();
    }

    private RequestBuilder createRequest(String url) {
        return client.request(url)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON);
    }

    private RequestBuilder createAuthenticatedRequest(String url) {

        return createRequest(url)
                .addBearerToken(getTokenFromSession())
                .accept(HeaderValues.APPLICATION_HAL_JSON);
    }

    private OAuth2Token getTokenFromSession() {
        return sessionStorage
                .get(StorageKeys.OAUTH_TOKEN, OAuth2Token.class)
                .orElseThrow(() -> new IllegalStateException("Cannot find token!"));
    }

    public URL getAuthorizeUrl(String state) {
        final String clientId = configuration.getClientId();

        final String oauthUrl = Urls.AUTH_URL;

        return new URL(oauthUrl)
                .queryParam(QueryKeys.CLIENT_ID, clientId)
                .queryParam(QueryKeys.SCOPE, QueryValues.SCOPE)
                .queryParam(QueryKeys.RESPONSE_TYPE, QueryValues.RESPONSE_TYPE)
                .queryParam(QueryKeys.STATE, state)
                .queryParam(QueryKeys.REDIRECT_URI, redirectUrl);
    }

    public OAuth2Token getToken(String code) {
        final String clientId = configuration.getClientId();
        final String clientSecret = configuration.getClientSecret();
        final String tokenUrl = Urls.BASE_URL + Endpoints.TOKEN;

        final TokenResponse response =
                createRequest(tokenUrl)
                        .body(
                                TokenRequest.builder()
                                        .clientId(clientId)
                                        .clientSecret(clientSecret)
                                        .code(code)
                                        .grantType(FormValues.GRANT_TYPE)
                                        .redirectUri(redirectUrl)
                                        .scope(FormValues.SCOPE)
                                        .build(),
                                MediaType.APPLICATION_FORM_URLENCODED)
                        .accept(MediaType.APPLICATION_JSON)
                        .post(TokenResponse.class);

        return OAuth2Token.create(
                response.getTokenType(),
                response.getToken(),
                response.getRefresh(),
                response.getExpiresIn());
    }

    public OAuth2Token refreshToken(String refreshToken) {
        final String tokenUrl = Urls.BASE_URL + Endpoints.TOKEN;

        final TokenResponse response =
                createRequest(tokenUrl)
                        .body(
                                RefreshTokenRequest.builder()
                                        .clientId(configuration.getClientId())
                                        .clientSecret(configuration.getClientSecret())
                                        .refreshToken(refreshToken)
                                        .grantType(FormValues.GRANT_TYPE)
                                        .redirectUri(redirectUrl)
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
        final String baseUrl = Urls.BASE_URL;
        final String accountsUrl = baseUrl + Endpoints.ACCOUNTS;

        return createAuthenticatedRequest(accountsUrl).get(GetAccountsResponse.class);
    }

    public GetBalancesResponse getBalanceForAccount(Account account) {
        final String baseUrl = Urls.BASE_URL;
        final String balancesUrl = baseUrl + account.getLinks().getBalances().getHref();

        return createAuthenticatedRequest(balancesUrl).get(GetBalancesResponse.class);
    }

    public GetTransactionsResponse getTransactionsForAccount(TransactionalAccount account) {
        final String baseUrl = Urls.BASE_URL;
        final String transactionsUrl =
                account.getFromTemporaryStorage(StorageKeys.ACCOUNT_LINKS, Links.class)
                        .map(links -> baseUrl + links.getTransactions().getHref())
                        .orElseThrow(IllegalStateException::new);

        return createAuthenticatedRequest(transactionsUrl).get(GetTransactionsResponse.class);
    }
}
