package se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import org.apache.http.HttpStatus;
import org.apache.http.cookie.Cookie;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.authenticator.entities.Saml2PostEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.authenticator.rpc.PasswordValidationRequest;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.authenticator.rpc.PasswordValidationResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.authenticator.rpc.ProfileResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.authenticator.rpc.Saml2AcsRequest;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.authenticator.rpc.TokensResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.entities.AppConfigEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.entities.BankEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.fetcher.loan.rpc.LoanDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.fetcher.transactionalaccounts.rpc.BanquePopulaireTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.rpc.BankConfigResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.rpc.ContractsResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.rpc.GeneralConfigrationResponse;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class BanquePopulaireApiClient {
    private static final AggregationLogger LOGGER = new AggregationLogger(BanquePopulaireApiClient.class);

    private final SessionStorage sessionStorage;
    private TinkHttpClient client;
    private BankEntity cachedBankEntity;
    private AppConfigEntity cachedAppConfig;
    private String bankId;

    public BanquePopulaireApiClient(TinkHttpClient client, SessionStorage sessionStorage, String bankId) {
        this.client = client;
        this.sessionStorage = sessionStorage;
        this.bankId = bankId;
    }

    public GeneralConfigrationResponse getConfiguration() {
        URL url = new URL(BanquePopulaireConstants.Urls.GENERAL_CONFIG)
                .queryParam(BanquePopulaireConstants.Query.BRAND, BanquePopulaireConstants.Query.BRAND_VALUE)
                .queryParam(BanquePopulaireConstants.Query.APP_TYPE, BanquePopulaireConstants.Query.APP_TYPE_VALUE);
        GeneralConfigrationResponse generalConfig = baseRequest(url)
                .get(GeneralConfigrationResponse.class);

        cachedBankEntity = generalConfig.getBankConfiguration().get(bankId);
        if (cachedBankEntity == null) {
            throw new IllegalStateException("No Bank configuration found");
        }

        sessionStorage.put(BanquePopulaireConstants.Storage.BANK_ENTITY, cachedBankEntity);
        getBankConfiguration();

        return generalConfig;
    }

    private void getBankConfiguration() {
        URL url = new URL(getBankEntity().getAnoBaseUrl() +
                getBankEntity().getApplicationAPIContextRoot() +
                BanquePopulaireConstants.Urls.BANK_CONFIG_PATH)
                .queryParam(BanquePopulaireConstants.Query.APP_TYPE, BanquePopulaireConstants.Query.APP_TYPE_VALUE)
                .queryParam(BanquePopulaireConstants.Query.APP_VERSION,
                        BanquePopulaireConstants.Query.APP_VERSION_VALUE)
                .queryParam(BanquePopulaireConstants.Query.BRAND, BanquePopulaireConstants.Query.BRAND_VALUE)
                .queryParam(BanquePopulaireConstants.Query.OS, BanquePopulaireConstants.Query.OS_VALUE);

        BankConfigResponse bankConfig = baseRequest(url)
                .get(BankConfigResponse.class);

        cachedAppConfig = bankConfig.getAppConfig();
        sessionStorage.put(BanquePopulaireConstants.Storage.APP_CONFIGURATION, cachedAppConfig);

        // don't be greedy cookies for everyone!!!
        addCookiesForAuthDomain(cachedAppConfig.getAuthBaseUrl());
    }

    public HttpResponse initiateSession() {
        return baseRequest(getAppConfigEntity().getAuthBaseUrl() +
                getBankEntity().getApplicationAPIContextRoot() +
                BanquePopulaireConstants.Urls.INITIATE_SESSION_PATH)
                .header(HttpHeaders.CONTENT_TYPE, BanquePopulaireConstants.Headers.CONTENT_TYPE_JSON_UTF8)
                .get(HttpResponse.class);
    }

    public PasswordValidationResponse authenticate(String baseAuthUrl, PasswordValidationRequest passwordValidationRequest) {
        return baseRequest(baseAuthUrl + getAppConfigEntity().getWebSSOv3WebAPIStepURL())
                        .header(HttpHeaders.CONTENT_TYPE, BanquePopulaireConstants.Headers.CONTENT_TYPE_JSON_UTF8)
                        .post(PasswordValidationResponse.class, passwordValidationRequest);
    }

    public ProfileResponse authenticateSaml2(PasswordValidationResponse passwordValidationResponse) {
        Saml2PostEntity saml2postEntity = passwordValidationResponse.getResponse().getSaml2Post();
        Saml2AcsRequest saml2AcsRequest = Saml2AcsRequest.create(saml2postEntity.getSamlResponse());

        return baseRequest(saml2postEntity.getAction())
                .body(saml2AcsRequest, MediaType.APPLICATION_FORM_URLENCODED)
                .post(ProfileResponse.class);
    }

    public TokensResponse getTokens() {

        return baseRequest(getAppConfigEntity().getAuthBaseUrl() +
                getAppConfigEntity().getWebAPI2().getAuthAccessTokenURL())
                .header(HttpHeaders.CONTENT_TYPE, BanquePopulaireConstants.Headers.CONTENT_TYPE_JSON_UTF8)
                .header(HttpHeaders.AUTHORIZATION, BanquePopulaireConstants.Authentication.SCOPES_IN_AUTH_HEADER)
                .post(TokensResponse.class);
    }

    public ContractsResponse getAccountContracts() {
        HttpResponse rawResponse = null;
        try {
            rawResponse = baseRequest(getAppConfigEntity().getAuthBaseUrl() +
                    getBankEntity().getApplicationAPIContextRoot() + BanquePopulaireConstants.Urls.ACCOUNTS_PATH)
                    .header(HttpHeaders.CONTENT_TYPE, BanquePopulaireConstants.Headers.CONTENT_TYPE_JSON_UTF8)
                    .get(HttpResponse.class);
            return rawResponse.getBody(ContractsResponse.class);
        } catch (Exception e) {
            logPaginationResponse(rawResponse);
            throw e;
        }
    }

    public BanquePopulaireTransactionsResponse getAccountTransactions(TransactionalAccount account,
            String paginationKey) {

        URL transactionsUrl = new URL(getAppConfigEntity().getAuthBaseUrl() +
                getBankEntity().getApplicationAPIContextRoot() +
                BanquePopulaireConstants.Urls.TRANSACTIONS_PATH)
                .parameter(BanquePopulaireConstants.Fetcher.ACCOUNT_PARAMETER, account.getBankIdentifier())
                .queryParam(BanquePopulaireConstants.Query.PAGE_KEY, Optional.ofNullable(paginationKey)
                        .orElse(""))
                .queryParam(BanquePopulaireConstants.Query.TRANSACTION_STATUS,
                        BanquePopulaireConstants.Query.TRANSACTION_STATUS_VALUE);

        // this try catch is for parsing the response as I suspect we can have a different response object for
        // pagination. We have not seen any pagination response yet but the smali code looks like it differentiates
        // between list-response and pagination response
        HttpResponse rawResponse = null;
        try {
            rawResponse = baseRequest(transactionsUrl)
                    .header(HttpHeaders.CONTENT_TYPE, BanquePopulaireConstants.Headers.CONTENT_TYPE_JSON_UTF8)
                    .get(HttpResponse.class);

            return rawResponse.getBody(BanquePopulaireTransactionsResponse.class);
        } catch (Exception e) {
            logPaginationResponse(rawResponse);
            throw e;
        }
    }

    public ContractsResponse getAllContracts() {
        HttpResponse rawResponse = null;
        // this try catch is for parsing the response as I suspect we can have a different response object for
        // pagination. We have not seen any pagination response yet but the smali code looks like it differentiates
        // between list-response and pagination response
        try {
            rawResponse = baseRequest(getAppConfigEntity().getAuthBaseUrl() +
                    getBankEntity().getApplicationAPIContextRoot() + BanquePopulaireConstants.Urls.CONTRACTS_PATH)
                    .header(HttpHeaders.CONTENT_TYPE, BanquePopulaireConstants.Headers.CONTENT_TYPE_JSON_UTF8)
                    .get(HttpResponse.class);
            return rawResponse.getBody(ContractsResponse.class);
        } catch (Exception e) {
            if (rawResponse != null && rawResponse.hasBody()) {
                LOGGER.warnExtraLong(rawResponse.getBody(String.class), BanquePopulaireConstants.LogTags.PAGINATION_RESPONSE);
            }
            throw e;
        }
    }

    public LoanDetailsResponse getLoanAccountDetails(String loanAccountIdentifier) {
        URL url = new URL(getAppConfigEntity().getAuthBaseUrl() +
                getBankEntity().getApplicationAPIContextRoot() + BanquePopulaireConstants.Urls.CONTRACT_DETAILS_PATH)
                .parameter(BanquePopulaireConstants.Fetcher.ACCOUNT_PARAMETER, loanAccountIdentifier);

        return baseRequest(url)
                .header(HttpHeaders.CONTENT_TYPE, BanquePopulaireConstants.Headers.CONTENT_TYPE_JSON_UTF8)
                .get(LoanDetailsResponse.class);
    }

    public String getAllCards() {
        TokensResponse tokens = sessionStorage.get(BanquePopulaireConstants.Storage.TOKENS, TokensResponse.class)
                .orElseThrow(() -> new IllegalStateException("No autorization token found"));

        URL url = new URL(getAppConfigEntity().getAuthBaseUrl()
                + getAppConfigEntity().getWebAPI2().getAuthBusinessContextRoot()
                + getAppConfigEntity().getWebAPI2().getEntryPoint(BanquePopulaireConstants.Fetcher.CARD_ENTRY_POINT))
                .queryParam(BanquePopulaireConstants.Query.CARD_STATUS_CODES
                        , BanquePopulaireConstants.Query.CARD_STATUS_CODES_VALUE);

        return baseRequest(url)
                .header(HttpHeaders.AUTHORIZATION,
                        String.format("%s %s", tokens.getTokenType(), tokens.getAccessToken()))
                .get(String.class);
    }

    public boolean keepAlive() {
        HttpResponse keepAliveResponse = baseRequest(getAppConfigEntity().getAuthBaseUrl() +
                getAppConfigEntity().getKeepAlive().getWebappURL())
                .get(HttpResponse.class);
        if (keepAliveResponse.getStatus() != HttpStatus.SC_OK) {
            return false;
        }

        keepAliveResponse = baseRequest(getAppConfigEntity().getAuthBaseUrl() +
                getAppConfigEntity().getKeepAlive().getWebSSOv3URL())
                .get(HttpResponse.class);

        if (keepAliveResponse.getStatus() != HttpStatus.SC_OK) {
            return false;
        }

        String response = keepAliveResponse.getBody(String.class);

        return response.toLowerCase().contains(BanquePopulaireConstants.TRUE);
    }

    public void logout() {
        try {
            baseRequest(getAppConfigEntity().getAuthBaseUrl() +
                    getBankEntity().getApplicationAPIContextRoot() +
                    BanquePopulaireConstants.Urls.LOGOUT_PATH)
                    .get(HttpResponse.class);

            baseRequest(getAppConfigEntity().getAuthBaseUrl() +
                    getAppConfigEntity().getWebSSOv3LogoutURL())
                    .get(HttpResponse.class);
        } catch (Exception e) {
            LOGGER.info("Error logging out", e);
        }
    }

    private void logPaginationResponse(HttpResponse rawResponse) {
        if (rawResponse != null && rawResponse.hasBody()) {
            LOGGER.warnExtraLong(rawResponse.getBody(String.class),
                    BanquePopulaireConstants.LogTags.PAGINATION_RESPONSE);
        }
    }

    private void addCookiesForAuthDomain(String newDomainUri) {
        try {
            URI uri = new URI(newDomainUri);
            String newDomain = uri.getHost();
            List<Cookie> allCookies = client.getCookies();
            // we get some cookies like JSESSIONID which will force backend to mistake us for a webpage, so clear
            client.clearCookies();

            allCookies.stream()
                    .filter(cookie -> BanquePopulaireConstants.Cookies.CIM_SESSION_ID.equalsIgnoreCase(cookie.getName())
                            ||
                            BanquePopulaireConstants.Cookies.CYBERPLUS_HYBRID.equalsIgnoreCase(cookie.getName()) ||
                            BanquePopulaireConstants.Cookies.CIM_XITI_ID.equalsIgnoreCase(cookie.getName()))
                    .forEach(cookie -> {
                        client.addCookie(cookie, copyCookieToDomain(cookie, newDomain));
                    });

            client.addCookie(createCookieForDomain(BanquePopulaireConstants.Cookies.NAV,
                    BanquePopulaireConstants.Cookies.NAV_VALUE, newDomain));
            client.addCookie(createCookieForDomain(BanquePopulaireConstants.Cookies.RPALTBE,
                    BanquePopulaireConstants.Cookies.RPALTBE_VALUE, newDomain));

        } catch (URISyntaxException e) {
            throw new IllegalStateException("Failed to add cookies for auth-domain", e);
        }
    }

    private org.apache.http.cookie.Cookie createCookieForDomain(String name, String value, String domain) {
        org.apache.http.impl.cookie.BasicClientCookie newCookie =
                new org.apache.http.impl.cookie.BasicClientCookie(name, value);
        newCookie.setDomain(domain);
        newCookie.setPath("/");
        newCookie.setSecure(true);

        return newCookie;
    }

    private org.apache.http.cookie.Cookie copyCookieToDomain(org.apache.http.cookie.Cookie clientCookie,
            String domain) {
        org.apache.http.impl.cookie.BasicClientCookie newCookie =
                new org.apache.http.impl.cookie.BasicClientCookie(clientCookie.getName(), clientCookie.getValue());
        newCookie.setDomain(domain);
        newCookie.setPath(clientCookie.getPath());
        newCookie.setExpiryDate(clientCookie.getExpiryDate());
        newCookie.setSecure(clientCookie.isSecure());

        return newCookie;
    }

    private BankEntity getBankEntity() {
        return Optional.ofNullable(cachedBankEntity).orElseGet(() -> cachedBankEntity =
                sessionStorage.get(BanquePopulaireConstants.Storage.BANK_ENTITY,
                        BankEntity.class).orElseThrow(() -> new IllegalStateException("No BankEntity available")));
    }

    private AppConfigEntity getAppConfigEntity() {
        return Optional.ofNullable(cachedAppConfig).orElseGet(() -> cachedAppConfig =
                sessionStorage.get(BanquePopulaireConstants.Storage.APP_CONFIGURATION,
                        AppConfigEntity.class)
                        .orElseThrow(() -> new IllegalStateException("No AppConfigEntity available")));
    }

    private RequestBuilder baseRequest(String url) {
        return baseRequest(new URL(url));
    }

    private RequestBuilder baseRequest(URL url) {
        return client.request(url)
                .header(BanquePopulaireConstants.Headers.IBP_WEBAPI_CALLERID_NAME,
                        BanquePopulaireConstants.Headers.IBP_WEBAPI_CALLERID)
                .header(HttpHeaders.ACCEPT, MediaType.WILDCARD)
                .header(HttpHeaders.ACCEPT_LANGUAGE, BanquePopulaireConstants.Headers.ACCEPT_LANGUAGE)
                .header(HttpHeaders.USER_AGENT, BanquePopulaireConstants.Headers.USER_AGENT)
                .header(HttpHeaders.CACHE_CONTROL, BanquePopulaireConstants.Headers.CACHE_NO_TRANSFORM);
    }
}
