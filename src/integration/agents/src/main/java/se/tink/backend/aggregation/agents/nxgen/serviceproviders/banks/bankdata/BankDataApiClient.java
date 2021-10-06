package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata;

import java.util.Locale;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.BankDataConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.BankDataConstants.HeaderValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.BankDataConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.BankDataConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.BankDataConstants.Storage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.BankDataConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.authenticator.rpc.ChallengeResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.authenticator.rpc.ClientRegistrationRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.authenticator.rpc.ClientRegistrationResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.authenticator.rpc.OAuthResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.identity.rpc.IdentityResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.investment.rpc.InvestmentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.loan.entities.HomesEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.loan.rpc.MortgageDTO;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.loan.rpc.MortgageDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.loan.rpc.MortgageResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.transactionalaccount.rpc.AccountResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.transactionalaccount.rpc.TransactionResponse;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.form.Form;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

@RequiredArgsConstructor
public class BankDataApiClient {
    private final TinkHttpClient client;
    private final SessionStorage sessionStorage;
    private final RandomValueGenerator randomValueGenerator;
    private final BankDataConfiguration configuration;
    private String bidCorrId = "";

    public HttpResponse nemIdInit(String codeChallenge) {
        HttpResponse httpResponse =
                client.request(configuration.getAuthHost() + Urls.INIT_AUTH)
                        .queryParam(QueryKeys.RESPONSE_TYPE, QueryValues.RESPONSE_TYPE)
                        .queryParam(QueryKeys.CLIENT_ID, QueryValues.CLIENT_ID)
                        .queryParam(QueryKeys.SCOPE, QueryValues.SCOPE)
                        .queryParam(QueryKeys.REDIRECT_URI, configuration.getRedirectUri())
                        .queryParam(QueryKeys.RESPONSE_MODE, QueryValues.RESPONSE_MODE)
                        .queryParam(QueryKeys.UI_LOCALES, QueryValues.UI_LOCALES)
                        .queryParam(QueryKeys.ENROLLMENT_CHALLENGE, codeChallenge)
                        .acceptLanguage(Locale.US)
                        .header(HttpHeaders.USER_AGENT, configuration.getUserAgent())
                        .accept(HeaderValues.ACCEPT_HTML)
                        .get(HttpResponse.class);

        // HTTP 302 redirects has been disabled for this agent which is why we have to follow
        // redirects manually here
        String url = httpResponse.getHeaders().getFirst(HttpHeaders.LOCATION);
        httpResponse = client.request(url).get(HttpResponse.class);

        url = httpResponse.getHeaders().getFirst(HttpHeaders.LOCATION);
        return client.request(url).get(HttpResponse.class);
    }

    public HttpResponse validateNemIdToken(String token) {
        return buildTokenRequest(configuration.getAuthHost() + Urls.VALIDATE_NEMID)
                .post(HttpResponse.class, "response=" + EncodingUtils.encodeUrl(token));
    }

    public String fetchToken(String uri, Form form) {
        final HttpResponse httpResponse =
                buildTokenRequest(configuration.getAuthHost() + uri)
                        .post(HttpResponse.class, form.serialize());

        return httpResponse.getHeaders().getFirst(HttpHeaders.LOCATION);
    }

    private RequestBuilder buildTokenRequest(String url) {
        return client.request(url)
                .header(HeaderKeys.ORIGIN, configuration.getAuthHost())
                .header(HeaderKeys.REFERER, configuration.getReferer())
                .header(HttpHeaders.USER_AGENT, configuration.getUserAgent())
                .type(MediaType.APPLICATION_FORM_URLENCODED)
                .acceptLanguage(Locale.US)
                .accept(HeaderValues.ACCEPT_HTML);
    }

    public ClientRegistrationResponse fetchClientSecret(String token) {
        return client.request(configuration.getAuthHost() + Urls.CLIENT_SECRET)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .post(ClientRegistrationResponse.class, new ClientRegistrationRequest());
    }

    public OAuthResponse fetchAccessToken(String clientId, String clientSecret, Form oauthForm) {
        final String corrId = randomValueGenerator.generateUuidWithTinkTag();

        return client.request(configuration.getAuthHost() + Urls.OAUTH_TOKEN)
                .header(HeaderKeys.CORR_ID, corrId)
                .header(HttpHeaders.USER_AGENT, configuration.getUserAgent())
                .type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                .addBasicAuth(clientId, clientSecret)
                .post(OAuthResponse.class, oauthForm.serialize());
    }

    public ChallengeResponse fetchChallengeCode(String kid) {
        return client.request(configuration.getAuthHost() + Urls.AUTH_CHALLENGE + kid)
                .accept(MediaType.WILDCARD_TYPE)
                .header(HttpHeaders.USER_AGENT, configuration.getUserAgent())
                .get(ChallengeResponse.class);
    }

    public IdentityResponse fetchIdentityData() {
        return buildRequest(configuration.getHost() + Urls.FETCH_IDENTITY)
                .get(IdentityResponse.class);
    }

    public AccountResponse fetchAccounts() {
        return sessionStorage
                .get(Storage.ACCOUNT_RESPONSE, AccountResponse.class)
                .orElse(fetchAccountsResponse());
    }

    private AccountResponse fetchAccountsResponse() {
        final AccountResponse accountResponse =
                buildRequest(configuration.getHost() + Urls.FETCH_ACCOUNTS)
                        .get(AccountResponse.class);
        sessionStorage.put(Storage.ACCOUNT_RESPONSE, accountResponse);

        return accountResponse;
    }

    public TransactionResponse fetchTransactions(String publicId, int page) {
        return buildRequest(configuration.getHost() + Urls.FETCH_TRANSACTIONS)
                .queryParam(QueryKeys.PUBLIC_ID, publicId)
                .queryParam(QueryKeys.PAGE, Integer.toString(page))
                .accept(HeaderValues.ACCEPT_JSON)
                .get(TransactionResponse.class);
    }

    public InvestmentResponse fetchInvestments() {
        return buildRequest(configuration.getHost() + Urls.FETCH_INVESTMENTS)
                .queryParam(QueryKeys.CLASSIFICATIONS, QueryValues.CLASSIFICATIONS)
                .queryParam(QueryKeys.LISTINGS, QueryValues.LISTINGS)
                .accept(HeaderValues.ACCEPT_JSON)
                .get(InvestmentResponse.class);
    }

    public MortgageResponse fetchMortgages() {
        return buildRequest(configuration.getHost() + Urls.FETCH_MORTGAGES)
                .accept(HeaderValues.ACCEPT_JSON)
                .get(MortgageResponse.class);
    }

    public MortgageDTO fetchMortgageDetails(HomesEntity homesEntity, String mortgageId) {
        final MortgageDetailsResponse mortgageDetailsResponse =
                buildRequest(
                                configuration.getHost()
                                        + Urls.FETCH_MORTGAGES
                                        + homesEntity.getPropertyNo()
                                        + '/'
                                        + mortgageId)
                        .accept(HeaderValues.ACCEPT_JSON)
                        .get(MortgageDetailsResponse.class);

        return new MortgageDTO(mortgageDetailsResponse, homesEntity, mortgageId);
    }

    private RequestBuilder buildRequest(String url) {
        if (bidCorrId.isEmpty()) {
            bidCorrId = randomValueGenerator.generateUuidWithTinkTag();
        }

        return client.request(url)
                .header(HeaderKeys.BD_CORRELATION, bidCorrId)
                .header(HeaderKeys.API_KEY, configuration.getApiKey())
                .header(HeaderKeys.APP_VERSION, configuration.getAppVersion())
                .header(
                        HeaderKeys.AUTHORIZATION,
                        "Bearer " + sessionStorage.get(Storage.ACCESS_TOKEN))
                .accept(MediaType.WILDCARD_TYPE);
    }

    public String validateVersion(String correlationId) {
        return buildServerStatusRequest(
                        configuration.getHost() + Urls.VALIDATE_VERSION, correlationId)
                .header(HeaderKeys.BUILD_NUMBER, configuration.getBuildNumber())
                .get(String.class);
    }

    public String serverStatus(String correlationId) {
        return buildServerStatusRequest(configuration.getHost() + Urls.SERVER_STATUS, correlationId)
                .get(String.class);
    }

    public String generalHealth(String correlationId) {
        return buildServerStatusRequest(
                        configuration.getHost() + Urls.GENERAL_HEALTH, correlationId)
                .get(String.class);
    }

    private RequestBuilder buildServerStatusRequest(String url, String correlationId) {
        return client.request(url)
                .header(HttpHeaders.USER_AGENT, configuration.getUserAgent())
                .header(HeaderKeys.API_KEY, configuration.getApiKey())
                .header(HeaderKeys.CORRELATION_ID, correlationId)
                .accept(MediaType.WILDCARD_TYPE);
    }
}
