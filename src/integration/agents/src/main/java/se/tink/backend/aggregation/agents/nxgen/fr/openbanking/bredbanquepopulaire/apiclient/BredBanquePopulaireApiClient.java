package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.apiclient;

import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.BredBanquePopulaireConstants;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.authenticator.rpc.RefreshRequest;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.authenticator.rpc.TokenRequest;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.configuration.BredBanquePopulaireConfiguration;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.fetcher.identity.rpc.EndUserIdentityResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.fetcher.transacitons.rpc.TransactionResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.fetcher.transactionalaccount.entity.CustomerConsent;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.fetcher.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.fetcher.transactionalaccount.rpc.BalancesResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.signature.BredBanquePopulaireHeaderGenerator;
import se.tink.backend.aggregation.api.Psd2Headers;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.request.HttpMethod;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class BredBanquePopulaireApiClient {
    private final TinkHttpClient httpClient;
    private final PersistentStorage persistentStorage;
    private final BredBanquePopulaireConfiguration bredBanquePopulaireConfiguration;
    private final RandomValueGenerator randomValueGenerator;
    private final BredBanquePopulaireHeaderGenerator bredBanquePopulaireSignatureHeaderGenerator;
    private final String redirectUrlBase;
    private String state;

    public BredBanquePopulaireApiClient(
            TinkHttpClient httpClient,
            PersistentStorage persistentStorage,
            AgentConfiguration<BredBanquePopulaireConfiguration> agentConfiguration,
            RandomValueGenerator randomValueGenerator,
            BredBanquePopulaireHeaderGenerator bredBanquePopulaireSignatureHeaderGenerator) {
        this.httpClient = httpClient;
        this.persistentStorage = persistentStorage;
        this.bredBanquePopulaireConfiguration =
                agentConfiguration.getProviderSpecificConfiguration();
        this.randomValueGenerator = randomValueGenerator;
        this.bredBanquePopulaireSignatureHeaderGenerator =
                bredBanquePopulaireSignatureHeaderGenerator;
        this.redirectUrlBase = agentConfiguration.getRedirectUrl();
    }

    public URL getAuthorizeUrl(String state) {
        this.state = state;
        return httpClient
                .request(new URL(BredBanquePopulaireConstants.Urls.OAUTH_URL))
                .queryParam(
                        BredBanquePopulaireConstants.QueryKeys.CLIENT_ID,
                        bredBanquePopulaireConfiguration.getClientId())
                .queryParam(
                        BredBanquePopulaireConstants.QueryKeys.RESPONSE_TYPE,
                        BredBanquePopulaireConstants.QueryValues.RESPONSE_TYPE)
                .queryParam(
                        BredBanquePopulaireConstants.QueryKeys.SCOPE,
                        BredBanquePopulaireConstants.QueryValues.SCOPE)
                .queryParam(
                        BredBanquePopulaireConstants.QueryKeys.REDIRECT_URI,
                        getRedirectUrlWithState(this.state))
                .getUrl();
    }

    public OAuth2Token exchangeAuthorizationToken(String code) {
        final TokenRequest tokenRequest =
                new TokenRequest(
                        BredBanquePopulaireConstants.QueryValues.AUTHORIZATION_CODE,
                        code,
                        getRedirectUrlWithState(this.state),
                        bredBanquePopulaireConfiguration.getClientId());

        return exchangeToken(tokenRequest).toOauthToken();
    }

    public OAuth2Token exchangeRefreshToken(String refreshToken) {
        final RefreshRequest refreshRequest =
                new RefreshRequest(bredBanquePopulaireConfiguration.getClientId(), refreshToken);

        return exchangeToken(refreshRequest).toOauthToken();
    }

    public AccountsResponse fetchAccounts() {
        return buildRequestWithSignature(
                        new URL(BredBanquePopulaireConstants.Urls.FETCH_ACCOUNTS), HttpMethod.GET)
                .get(AccountsResponse.class);
    }

    public BalancesResponse fetchBalances(String accountResourceId) {
        final String path =
                String.format(BredBanquePopulaireConstants.Urls.FETCH_BALANCES, accountResourceId);

        return buildRequestWithSignature(new URL(path), HttpMethod.GET).get(BalancesResponse.class);
    }

    public void recordCustomerConsent(CustomerConsent customerConsent) {
        final String digest =
                bredBanquePopulaireSignatureHeaderGenerator.getDigestHeaderValue(customerConsent);

        buildRequestWithSignature(
                        new URL(BredBanquePopulaireConstants.Urls.CUSTOMERS_CONSENTS),
                        HttpMethod.PUT)
                .body(customerConsent, MediaType.APPLICATION_JSON_TYPE)
                .header(Psd2Headers.Keys.DIGEST, digest)
                .put();
    }

    public TransactionResponse getTransactions(String accountResourceId) {
        final String path =
                String.format(
                        BredBanquePopulaireConstants.Urls.FETCH_TRANSACTIONS, accountResourceId);
        return fetchTransactions(path);
    }

    public TransactionResponse getTransactions(String accountResourceId, int page) {
        final String path =
                String.format(
                        BredBanquePopulaireConstants.Urls.FETCH_TRANSACTIONS + "?page=" + page,
                        accountResourceId);
        return fetchTransactions(path);
    }

    private TransactionResponse fetchTransactions(String url) {
        return buildRequestWithSignature(new URL(url), HttpMethod.GET)
                .get(TransactionResponse.class);
    }

    public EndUserIdentityResponse getEndUserIdentity() {
        return buildRequestWithSignature(
                        new URL(BredBanquePopulaireConstants.Urls.FETCH_IDENTITY_DATA),
                        HttpMethod.GET)
                .get(EndUserIdentityResponse.class);
    }

    private TokenResponse exchangeToken(Object requestBody) {
        return httpClient
                .request(new URL(BredBanquePopulaireConstants.Urls.GET_TOKEN))
                .body(requestBody, MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON)
                .post(TokenResponse.class);
    }

    private RequestBuilder buildRequestWithSignature(final URL url, HttpMethod httpMethod) {
        final String requestId = randomValueGenerator.getUUID().toString();
        final OAuth2Token token = getToken();
        final String signature =
                bredBanquePopulaireSignatureHeaderGenerator.buildSignatureHeader(
                        httpMethod, url, requestId);

        return httpClient
                .request(url)
                .addBearerToken(token)
                .header(Psd2Headers.Keys.X_REQUEST_ID, requestId)
                .header(Psd2Headers.Keys.SIGNATURE, signature)
                .header(
                        BredBanquePopulaireConstants.QueryKeys.OCP_APIM_SUBSCRIPTION_KEY,
                        bredBanquePopulaireConfiguration.getOcpApimSubscriptionKey());
    }

    private String getRedirectUrlWithState(String state) {
        return String.format(BredBanquePopulaireConstants.QueryKeys.STATE, redirectUrlBase, state);
    }

    private OAuth2Token getToken() {
        return persistentStorage
                .get(BredBanquePopulaireConstants.StorageKeys.OAUTH_TOKEN, OAuth2Token.class)
                .orElseThrow(
                        () -> new IllegalStateException("Cannot find token in PersistentStorage."));
    }
}
