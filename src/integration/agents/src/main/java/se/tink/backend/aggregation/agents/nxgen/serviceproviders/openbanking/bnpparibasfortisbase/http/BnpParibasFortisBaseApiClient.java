package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibasfortisbase.http;

import java.util.Optional;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibasfortisbase.BnpParibasFortisBaseConstants.Endpoints;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibasfortisbase.BnpParibasFortisBaseConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibasfortisbase.BnpParibasFortisBaseConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibasfortisbase.BnpParibasFortisBaseConstants.HeaderValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibasfortisbase.BnpParibasFortisBaseConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibasfortisbase.BnpParibasFortisBaseConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibasfortisbase.BnpParibasFortisBaseConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibasfortisbase.authenticator.rpc.RefreshTokenRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibasfortisbase.authenticator.rpc.TokenRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibasfortisbase.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibasfortisbase.configuration.BnpParibasFortisBaseBankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibasfortisbase.configuration.BnpParibasFortisBaseConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibasfortisbase.transactionalaccount.entity.account.Account;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibasfortisbase.transactionalaccount.entity.account.Links;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibasfortisbase.transactionalaccount.entity.balance.GetBalancesResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibasfortisbase.transactionalaccount.rpc.GetAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibasfortisbase.transactionalaccount.rpc.GetTransactionsResponse;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class BnpParibasFortisBaseApiClient {

    private final TinkHttpClient client;
    private final SessionStorage sessionStorage;
    private BnpParibasFortisBaseConfiguration configuration;
    private final String redirectUrl;
    private final BnpParibasFortisBaseBankConfiguration bnpParibasFortisBaseBankConfiguration;

    public BnpParibasFortisBaseApiClient(
            TinkHttpClient client,
            SessionStorage sessionStorage,
            AgentConfiguration<BnpParibasFortisBaseConfiguration> agentConfiguration,
            BnpParibasFortisBaseBankConfiguration bnpParibasFortisBaseBankConfiguration) {
        this.client = client;
        this.sessionStorage = sessionStorage;
        this.configuration = agentConfiguration.getProviderSpecificConfiguration();
        this.redirectUrl = agentConfiguration.getRedirectUrl();
        this.bnpParibasFortisBaseBankConfiguration = bnpParibasFortisBaseBankConfiguration;
    }

    private RequestBuilder createAuthenticatedRequest(String url) {

        return client.request(url)
                .type(MediaType.APPLICATION_JSON)
                .addBearerToken(getTokenFromSession())
                .header(HeaderKeys.USER_AGENT, HeaderValues.TINK)
                .accept(HeaderValues.APPLICATION_HAL_JSON);
    }

    private OAuth2Token getTokenFromSession() {
        return sessionStorage
                .get(StorageKeys.OAUTH_TOKEN, OAuth2Token.class)
                .orElseThrow(() -> new IllegalStateException("Cannot find token!"));
    }

    public URL getAuthorizeUrl(String state) {
        final String clientId = configuration.getClientId();

        final String oauthUrl = bnpParibasFortisBaseBankConfiguration.getBaseAuth();

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
        final String tokenUrl =
                bnpParibasFortisBaseBankConfiguration.getBaseUrl() + Endpoints.TOKEN;

        final TokenResponse response =
                client.request(tokenUrl)
                        .body(
                                TokenRequest.builder()
                                        .clientId(clientId)
                                        .clientSecret(clientSecret)
                                        .code(code)
                                        .grantType(FormValues.GRANT_TYPE)
                                        .redirectUri(redirectUrl)
                                        .scope(FormValues.SCOPE)
                                        .build()
                                        .getBodyValue(),
                                MediaType.APPLICATION_FORM_URLENCODED)
                        .post(TokenResponse.class);

        return OAuth2Token.create(
                response.getTokenType(),
                response.getToken(),
                response.getRefresh(),
                response.getExpiresIn());
    }

    public OAuth2Token refreshToken(String refreshToken) {
        final String tokenUrl =
                bnpParibasFortisBaseBankConfiguration.getBaseUrl() + Endpoints.TOKEN;

        final TokenResponse response =
                client.request(tokenUrl)
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
                        .post(TokenResponse.class);

        return OAuth2Token.create(
                response.getTokenType(),
                response.getToken(),
                response.getRefresh(),
                response.getExpiresIn());
    }

    public GetAccountsResponse getAccounts() {
        final String baseUrl =
                bnpParibasFortisBaseBankConfiguration.getBaseUrl() + Endpoints.PSD2_BASE_PATH;
        final String accountsUrl = baseUrl + Endpoints.ACCOUNTS;

        return createAuthenticatedRequest(accountsUrl).get(GetAccountsResponse.class);
    }

    public GetBalancesResponse getBalanceForAccount(Account account) {
        final String baseUrl =
                bnpParibasFortisBaseBankConfiguration.getBaseUrl() + Endpoints.PSD2_BASE_PATH;
        final String balancesUrl = baseUrl + account.getLinks().getBalances().getHref();

        return createAuthenticatedRequest(balancesUrl).get(GetBalancesResponse.class);
    }

    public GetTransactionsResponse getTransactionsForAccount(
            String nextUrl, TransactionalAccount account) {
        final String baseUrl =
                bnpParibasFortisBaseBankConfiguration.getBaseUrl() + Endpoints.PSD2_BASE_PATH;

        final String transactionsUrl =
                Optional.ofNullable(nextUrl)
                        .map(url -> baseUrl + nextUrl)
                        .orElseGet(() -> getUrlFromStorage(account, baseUrl));

        return createAuthenticatedRequest(transactionsUrl).get(GetTransactionsResponse.class);
    }

    private String getUrlFromStorage(TransactionalAccount account, String baseUrl) {
        return account.getFromTemporaryStorage(StorageKeys.ACCOUNT_LINKS, Links.class)
                .map(links -> baseUrl + links.getTransactions().getHref())
                .orElseThrow(IllegalStateException::new);
    }
}
