package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea;

import static se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.NordeaDkConstants.HeaderValues.TEXT_HTML;

import java.util.AbstractMap.SimpleEntry;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.ws.rs.core.MediaType;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.NordeaDkConstants.FormKeys;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.NordeaDkConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.NordeaDkConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.NordeaDkConstants.HeaderValues;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.NordeaDkConstants.QueryParamKeys;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.NordeaDkConstants.QueryParamValues;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.NordeaDkConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.NordeaDkConstants.URLs;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.authenticator.rpc.AuthenticationsPatchRequest;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.authenticator.rpc.AuthenticationsPatchResponse;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.authenticator.rpc.CodeExchangeRequest;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.authenticator.rpc.CodeExchangeResponse;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.authenticator.rpc.NemIdParamsResponse;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.authenticator.rpc.NemidParamsRequest;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.authenticator.rpc.OauthCallbackResponse;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.creditcard.rpc.CreditCardDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.creditcard.rpc.CreditCardTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.creditcard.rpc.CreditCardsResponse;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.identitydata.rpc.IdentityDataResponse;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.investment.rpc.CustodyAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.loans.rpc.LoanDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.loans.rpc.LoansResponse;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.transactionalaccount.rpc.TransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.filters.ConnectionResetRetryFilter;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.filters.ServerErrorFilter;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.filters.ServerErrorRetryFilter;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.filter.filters.TimeoutFilter;
import se.tink.backend.aggregation.nxgen.http.form.Form;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.i18n.Catalog;
import src.integration.nemid.NemIdSupportedLanguageCode;

public class NordeaDkApiClient {

    private final SessionStorage sessionStorage;
    private final PersistentStorage persistentStorage;
    private final Catalog catalog;
    protected final TinkHttpClient client;

    public NordeaDkApiClient(
            SessionStorage sessionStorage,
            TinkHttpClient client,
            PersistentStorage persistentStorage,
            Catalog catalog) {
        this.sessionStorage = sessionStorage;
        this.client = client;
        this.persistentStorage = persistentStorage;
        this.catalog = catalog;

        this.client.addFilter(new TimeoutFilter());
        this.client.addFilter(new ConnectionResetRetryFilter());
        this.client.addFilter(new ServerErrorFilter());
        this.client.addFilter(new ServerErrorRetryFilter());
    }

    /**
     * This method returns the String representation of the url used in this call, so it can later
     * be used as Referer header in subsequent requests.
     */
    public String initOauth(String codeChallenge, String state, String nonce) {
        RequestBuilder request =
                client.request(URLs.NORDEA_AUTH_BASE_URL)
                        .queryParam(QueryParamKeys.CLIENT_ID, QueryParamValues.CLIENT_ID)
                        .queryParam(QueryParamKeys.CODE_CHALLENGE, codeChallenge)
                        .queryParam(
                                QueryParamKeys.CODE_CHALLENGE_METHOD,
                                QueryParamValues.CODE_CHALLENGE_METHOD)
                        .queryParam(QueryParamKeys.STATE, state)
                        .queryParam(QueryParamKeys.REDIRECT_URI, QueryParamValues.REDIRECT_URI)
                        .queryParam(QueryParamKeys.RESPONSE_TYPE, QueryParamValues.RESPONSE_TYPE)
                        .queryParam(QueryParamKeys.UI_LOCALES, QueryParamValues.UI_LOCALES)
                        .queryParam(QueryParamKeys.AV, QueryParamValues.AV)
                        .queryParam(QueryParamKeys.DM, QueryParamValues.DM)
                        .queryParam(QueryParamKeys.INSTALLED_APPS, QueryParamValues.INSTALLED_APPS)
                        .queryParam(QueryParamKeys.SCOPE, QueryParamValues.SCOPE)
                        .queryParam(QueryParamKeys.LOGIN_HINT, QueryParamValues.LOGIN_HINT)
                        .queryParam(QueryParamKeys.APP_CHANNEL, QueryParamValues.APP_CHANNEL)
                        .queryParam(QueryParamKeys.ADOBE_MC, QueryParamValues.ADOBE_MC)
                        .queryParam(QueryParamKeys.NONCE, nonce);
        String res = request.toString();
        request.header(HeaderKeys.HOST, HeaderValues.NORDEA_AUTH_HOST)
                .accept(TEXT_HTML)
                .acceptLanguage(HeaderValues.ACCEPT_LANGUAGE)
                .header(HeaderKeys.ACCEPT_ENCODING, HeaderValues.BR_GZIP_ENCODING)
                .get(String.class);
        return res;
    }

    public NemIdParamsResponse getNemIdParams(String codeChallenge, String state, String nonce) {
        NemidParamsRequest request =
                NemidParamsRequest.builder()
                        .withNonce(nonce)
                        .withState(state)
                        .withCodeChallenge(codeChallenge)
                        .build();

        // this value affects the language of nemID notification on user's device
        String userLanguageForNemIdRequest =
                NemIdSupportedLanguageCode.getFromCatalogOrDefault(catalog).getIsoLanguageCode();

        return baseIdentifyRequest(URLs.NORDEA_AUTH_BASE_URL + URLs.NEM_ID_AUTHENTICATION, false)
                .type(MediaType.APPLICATION_JSON)
                .header(HeaderKeys.ACCEPT_LANGUAGE, userLanguageForNemIdRequest)
                .post(NemIdParamsResponse.class, request);
    }

    public AuthenticationsPatchResponse authenticationsPatch(
            String response, String sessionId, String referer) {
        AuthenticationsPatchRequest request = new AuthenticationsPatchRequest(response);
        return baseIdentifyRequest(
                        URLs.NORDEA_AUTH_BASE_URL + URLs.NEM_ID_AUTHENTICATION + sessionId)
                .header(HeaderKeys.REFERER, referer)
                .header(HeaderKeys.APP_VERSION, HeaderValues.APP_VERSION)
                .header(HeaderKeys.DEVICE_MODEL, HeaderValues.DEVICE_MODEL)
                .type(MediaType.APPLICATION_JSON)
                .patch(AuthenticationsPatchResponse.class, request);
    }

    public CodeExchangeResponse codeExchange(String code, String referer) {
        CodeExchangeRequest request = new CodeExchangeRequest(code);
        try {
            return baseIdentifyRequest(URLs.NORDEA_AUTH_BASE_URL + URLs.AUTHORIZATION)
                    .header(HeaderKeys.REFERER, referer)
                    .type(MediaType.APPLICATION_JSON)
                    .post(CodeExchangeResponse.class, request);
        } catch (HttpResponseException e) {
            if (HttpStatus.SC_NOT_FOUND == e.getResponse().getStatus()) {
                throw LoginError.NO_ACCESS_TO_MOBILE_BANKING.exception();
            }
            throw e;
        }
    }

    public void redirect(String state, String code, String loginHint, String referer) {
        String body =
                Form.builder()
                        .put(FormKeys.CLIENT_ID, FormValues.CLIENT_ID)
                        .put(FormKeys.REDIRECT_URI, FormValues.REDIRECT_URI)
                        .put(FormKeys.STATE, state)
                        .put(FormKeys.CODE, code)
                        .put(FormKeys.LOGIN_HINT, loginHint)
                        .build()
                        .serialize();
        baseIdentifyRequest(URLs.NORDEA_AUTH_BASE_URL + "redirect")
                .type(MediaType.APPLICATION_FORM_URLENCODED)
                .header(HeaderKeys.REFERER, referer)
                .post(body);
    }

    public OauthCallbackResponse oauthCallback(String code, String codeVerifier) {
        String body =
                Form.builder()
                        .put(FormKeys.AUTH_METHOD, FormValues.AUTH_METHOD)
                        .put(FormKeys.CLIENT_ID, FormValues.CLIENT_ID)
                        .put(FormKeys.CODE, code)
                        .put(FormKeys.CODE_VERIFIER, codeVerifier)
                        .put(FormKeys.COUNTRY, FormValues.COUNTRY)
                        .put(FormKeys.GRANT_TYPE, FormValues.AUTHORIZATION_CODE)
                        .put(FormKeys.REDIRECT_URI, FormValues.REDIRECT_URI)
                        .put(FormKeys.SCOPE, FormValues.SCOPE)
                        .build()
                        .serialize();
        return basePrivateRequest(URLs.EXCHANGE_TOKEN).post(OauthCallbackResponse.class, body);
    }

    public OauthCallbackResponse exchangeRefreshToken(String refreshToken) {

        String body =
                Form.builder()
                        .put(FormKeys.CLIENT_ID, "NDHMDK")
                        .put(FormKeys.GRANT_TYPE, FormValues.REFRESH_TOKEN)
                        .put(FormKeys.REFRESH_TOKEN, refreshToken)
                        .build()
                        .serialize();

        return basePrivateRequest(URLs.EXCHANGE_TOKEN).post(OauthCallbackResponse.class, body);
    }

    private RequestBuilder baseIdentifyRequest(String url) {
        return baseIdentifyRequest(url, true);
    }

    private RequestBuilder baseIdentifyRequest(String url, boolean setDefaultLanguage) {
        RequestBuilder builder =
                client.request(url)
                        .header(HeaderKeys.ACCEPT_ENCODING, HeaderValues.BR_GZIP_ENCODING)
                        .header(HeaderKeys.PLATFORM_TYPE, HeaderKeys.PLATFORM_TYPE)
                        .header(HeaderKeys.ORIGIN, "https://identify.nordea.com")
                        .accept(MediaType.WILDCARD)
                        .header(HeaderKeys.HOST, HeaderValues.NORDEA_AUTH_HOST)
                        .header("x-device-ec", "0");
        if (setDefaultLanguage) {
            builder.header(HeaderKeys.ACCEPT_LANGUAGE, HeaderValues.ACCEPT_LANGUAGE);
        }
        return builder;
    }

    private RequestBuilder basePrivateRequest(String url) {
        return client.request(url)
                .header(HeaderKeys.ACCEPT_ENCODING, HeaderValues.BR_GZIP_ENCODING)
                .header(HeaderKeys.PLATFORM_TYPE, HeaderValues.PLATFORM_TYPE)
                .header(HeaderKeys.ACCEPT_LANGUAGE, HeaderValues.ACCEPT_LANGUAGE)
                .accept(MediaType.APPLICATION_JSON)
                .header(HeaderKeys.HOST, HeaderValues.NORDEA_PRIVATE_HOST)
                .header(HeaderKeys.APP_LANGUAGE, HeaderValues.APP_LANGUAGE)
                .header(HeaderKeys.PLATFORM_VERSION, HeaderValues.PLATFORM_VERSION)
                .header(HeaderKeys.APP_SEGMENT, HeaderValues.HOUSEHOLD_APP_SEGMENT)
                .header(HeaderKeys.DEVICE_ID, getOrGenerateDeviceId())
                .header(HeaderKeys.DEVICE_MODEL, HeaderValues.DEVICE_MODEL)
                .header(HeaderKeys.APP_COUNTRY, HeaderValues.APP_COUNTRY)
                .type(MediaType.APPLICATION_FORM_URLENCODED);
    }

    private String getOrGenerateDeviceId() {
        String deviceId = persistentStorage.get(StorageKeys.DEVICE_ID);
        if (deviceId == null) {
            deviceId = UUID.randomUUID().toString().toUpperCase();
            persistentStorage.put(StorageKeys.DEVICE_ID, deviceId);
        }
        return deviceId;
    }

    public AccountsResponse getAccounts() {
        return baseAuthorizedRequest(URLs.FETCH_ACCOUNTS).get(AccountsResponse.class);
    }

    public LoansResponse getLoans() {
        return baseAuthorizedRequest(URLs.FETCH_LOANS).get(LoansResponse.class);
    }

    public LoanDetailsResponse getLoanDetails(String loanId) {
        return baseAuthorizedRequest(URLs.FETCH_LOANS + "/" + loanId)
                .get(LoanDetailsResponse.class);
    }

    public TransactionsResponse getAccountTransactions(
            String accountId, String productCode, String continuationKey) {
        String url = String.format(URLs.FETCH_ACCOUNT_TRANSACTIONS_FORMAT, accountId);
        RequestBuilder request = baseAuthorizedRequest(url);
        if (continuationKey != null) {
            request.queryParam("continuation_key", continuationKey);
        } else {
            request.queryParam("product_code", productCode);
        }
        return request.get(TransactionsResponse.class);
    }

    public TransactionsResponse getAccountTransactions(
            String accountId,
            String productCode,
            String continuationKey,
            String dateFrom,
            String dateTo) {
        String url = String.format(URLs.FETCH_ACCOUNT_TRANSACTIONS_FORMAT, accountId);

        Map<String, String> queryParams =
                Stream.of(
                                new SimpleEntry<>("product_code", productCode),
                                new SimpleEntry<>("continuation_key", continuationKey),
                                new SimpleEntry<>("start_date", dateFrom),
                                new SimpleEntry<>("end_date", dateTo))
                        .filter(entry -> StringUtils.isNotEmpty(entry.getValue()))
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        RequestBuilder request = baseAuthorizedRequest(url).queryParams(queryParams);

        return request.get(TransactionsResponse.class);
    }

    public CreditCardsResponse fetchCreditCards() {
        return baseAuthorizedRequest(URLs.FETCH_CARDS).get(CreditCardsResponse.class);
    }

    public CreditCardDetailsResponse fetchCreditCardDetails(String cardId) {
        return baseAuthorizedRequest(String.format(URLs.FETCH_CARD_DETAILS_FORMAT, cardId))
                .get(CreditCardDetailsResponse.class);
    }

    public CreditCardTransactionsResponse fetchCreditCardTransactions(String cardId, int page) {
        return baseAuthorizedRequest(String.format(URLs.FETCH_CARD_TRANSACTIONS_FORMAT, cardId))
                .queryParam("page", String.valueOf(page))
                .queryParam("page_size", String.valueOf(100))
                .get(CreditCardTransactionsResponse.class);
    }

    public CustodyAccountsResponse fetchInvestments() {
        return baseAuthorizedRequest(URLs.FETCH_INVESTMENTS).get(CustodyAccountsResponse.class);
    }

    public IdentityDataResponse fetchIdentityData() {
        return baseAuthorizedRequest(URLs.FETCH_IDENTITY_DATA).get(IdentityDataResponse.class);
    }

    private RequestBuilder baseAuthorizedRequest(String url) {
        OAuth2Token token =
                sessionStorage
                        .get(StorageKeys.OAUTH_TOKEN, OAuth2Token.class)
                        .orElseThrow(BankServiceError.SESSION_TERMINATED::exception);

        String bearerToken = "Bearer " + token.getAccessToken();
        return basePrivateRequest(url)
                .header(HeaderKeys.AUTHORIZATION, bearerToken)
                .header(HeaderKeys.X_AUTHORIZATION, bearerToken);
    }
}
