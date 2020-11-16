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
        final String openbankStetVersion = configuration.getOpenbankStetVersion();
        final String organizationId = configuration.getOrganisationId();

        return createRequest(url)
                .addBearerToken(getTokenFromSession())
                .header(HeaderKeys.OPENBANK_STET_VERSION, openbankStetVersion)
                .header(HeaderKeys.ORGANIZATION_ID, organizationId)
                .header(HeaderKeys.REQUEST_ID, UUID.randomUUID().toString())
                .header(HeaderKeys.SIGNATURE, UUID.randomUUID().toString())
                .accept(HeaderKeys.APPLICATION_HAL_JSON);
    }

    private OAuth2Token getTokenFromSession() {
        return sessionStorage
                .get(StorageKeys.OAUTH_TOKEN, OAuth2Token.class)
                .orElseThrow(() -> new IllegalStateException("Cannot find token!"));
    }

    public URL getAuthorizeUrl(String state) {
        final String clientId = configuration.getClientId();

        final String redirectUri = redirectUrl;
        final String authBaseUrl = configuration.getAuthBaseUrl();
        final String oauthUrl = authBaseUrl + Urls.OAUTH;

        return createRequest(oauthUrl)
                .queryParam(QueryKeys.RESPONSE_TYPE, QueryValues.RESPONSE_TYPE)
                .queryParam(QueryKeys.CLIENT_ID, clientId)
                .queryParam(QueryKeys.REDIRECT_URI, redirectUri)
                .queryParam(QueryKeys.SCOPE, QueryValues.SCOPE)
                .queryParam(QueryKeys.STATE, state)
                .getUrl();
    }

    public OAuth2Token getToken(String code) {
        final String clientId = configuration.getClientId();
        final String clientSecret = configuration.getClientSecret();
        final String openbankStetVersion = configuration.getOpenbankStetVersion();
        final String organizationId = configuration.getOrganisationId();

        final String redirectUri = redirectUrl;
        final String authBaseUrl = configuration.getAuthBaseUrl();
        final String tokenUrl = authBaseUrl + Urls.TOKEN;

        final TokenResponse response =
                createRequest(tokenUrl)
                        .header(HeaderKeys.ORGANIZATION_ID, organizationId)
                        .header(HeaderKeys.OPENBANK_STET_VERSION, openbankStetVersion)
                        .body(
                                TokenRequest.builder()
                                        .clientId(clientId)
                                        .clientSecret(clientSecret)
                                        .code(code)
                                        .grantType(FormValues.GRANT_TYPE)
                                        .redirectUri(redirectUri)
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
        final String clientId = configuration.getClientId();
        final String clientSecret = configuration.getClientSecret();
        final String openbankStetVersion = configuration.getOpenbankStetVersion();
        final String organizationId = configuration.getOrganisationId();

        final String redirectUri = redirectUrl;
        final String authBaseUrl = configuration.getAuthBaseUrl();
        final String tokenUrl = authBaseUrl + Urls.TOKEN;

        final TokenResponse response =
                createRequest(tokenUrl)
                        .header(HeaderKeys.ORGANIZATION_ID, organizationId)
                        .header(HeaderKeys.OPENBANK_STET_VERSION, openbankStetVersion)
                        .body(
                                RefreshTokenRequest.builder()
                                        .clientId(clientId)
                                        .clientSecret(clientSecret)
                                        .refreshToken(refreshToken)
                                        .grantType(FormValues.GRANT_TYPE)
                                        .redirectUri(redirectUri)
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
        final String baseUrl = configuration.getApiBaseUrl();
        final String accountsUrl = baseUrl + Urls.ACCOUNTS;

        return createAuthenticatedRequest(accountsUrl).get(GetAccountsResponse.class);
    }

    public GetBalancesResponse getBalanceForAccount(Account account) {
        final String baseUrl = configuration.getApiBaseUrl();
        final String balancesUrl = baseUrl + account.getLinks().getBalances().getHref();

        return createAuthenticatedRequest(balancesUrl).get(GetBalancesResponse.class);
    }

    public GetTransactionsResponse getTransactionsForAccount(TransactionalAccount account) {
        final String baseUrl = configuration.getApiBaseUrl();
        final String transactionsUrl =
                account.getFromTemporaryStorage(StorageKeys.ACCOUNT_LINKS, Links.class)
                        .map(links -> baseUrl + links.getTransactions().getHref())
                        .orElseThrow(IllegalStateException::new);

        return createAuthenticatedRequest(transactionsUrl).get(GetTransactionsResponse.class);
    }
}
