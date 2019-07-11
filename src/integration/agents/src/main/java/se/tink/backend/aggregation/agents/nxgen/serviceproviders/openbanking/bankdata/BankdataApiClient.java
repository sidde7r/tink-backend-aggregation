package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata;

import com.google.api.client.util.Preconditions;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.BankdataConstants.Endpoints;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.BankdataConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.BankdataConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.BankdataConstants.Format;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.BankdataConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.BankdataConstants.HeaderValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.BankdataConstants.IdTags;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.BankdataConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.BankdataConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.BankdataConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.authenticator.rpc.AccessTokenRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.authenticator.rpc.ConsentAuthorizationRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.authenticator.rpc.ConsentAuthorizationResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.authenticator.rpc.ConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.authenticator.rpc.InitialTokenRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.authenticator.rpc.RefreshTokenRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.authenticator.rpc.TokenRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.configuration.BankdataConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.executor.payment.rpc.AuthorizePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.executor.payment.rpc.AuthorizePaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.executor.payment.rpc.BankdataErrorResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.executor.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.executor.payment.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.executor.payment.rpc.FetchCrossBorderPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.executor.payment.rpc.FetchDomesticPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.executor.payment.rpc.FetchSepaPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.executor.payment.rpc.GetPaymentDetails;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.fetcher.transactionalaccount.entities.BalanceEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.fetcher.transactionalaccount.rpc.AccountResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.fetcher.transactionalaccount.rpc.TransactionResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.util.DateUtils;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.utils.BerlinGroupUtils;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppStatus;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload.Android;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload.Ios;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.utils.OAuthUtils;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.payment.enums.PaymentType;
import tink.org.apache.http.HttpHeaders;

public final class BankdataApiClient {

    private static final long WAIT_FOR_SECONDS = 30;
    private final TinkHttpClient client;
    private final SessionStorage sessionStorage;
    private final SupplementalInformationHelper supplementalInformationHelper;
    private final String state;
    private BankdataConfiguration configuration;

    public BankdataApiClient(
            TinkHttpClient client,
            SessionStorage sessionStorage,
            SupplementalInformationHelper supplementalInformationHelper) {
        this.client = client;
        this.sessionStorage = sessionStorage;
        this.supplementalInformationHelper = supplementalInformationHelper;
        this.state = generateRandomState();
    }

    private static String generateRandomState() {
        return BerlinGroupUtils.getRequestId();
    }

    private BankdataConfiguration getConfiguration() {
        return Optional.ofNullable(configuration)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    protected void setConfiguration(BankdataConfiguration configuration) {
        this.configuration = configuration;
    }

    private RequestBuilder createRequest(URL url) {
        return client.request(url)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON);
    }

    private RequestBuilder createRequestInSession(URL url, String storageKey) {
        final OAuth2Token authToken = getTokenFromSession(storageKey);

        return createRequest(url).addBearerToken(authToken);
    }

    public CreatePaymentResponse createPayment(
            CreatePaymentRequest paymentRequest, PaymentType type) throws PaymentException {
        final String requestId = UUID.randomUUID().toString();
        final String productType = BankdataConstants.TYPE_TO_DOMAIN_MAPPER.get(type);

        URL url = new URL(configuration.getBaseUrl() + productType);

        try {
            CreatePaymentResponse response =
                    client.request(url)
                            .addBearerToken(getTokenFromSession(StorageKeys.INITIAL_TOKEN))
                            .header(HeaderKeys.X_API_KEY, configuration.getApiKey())
                            .header(HeaderKeys.X_REQUEST_ID, requestId)
                            .header(HeaderKeys.PSU_IP_ADDRESS, HeaderValues.PSU_IP_ADDRESS)
                            .body(paymentRequest.toData(), MediaType.APPLICATION_JSON_TYPE)
                            .post(CreatePaymentResponse.class);

            URL transactionDetailsURL =
                    new URL(
                            configuration.getBaseUrl()
                                    + Endpoints.PIS_PRODUCT
                                    + response.getDetailsLink());
            response.setPaymentStatus(
                    getPaymentDetails(transactionDetailsURL).getTransactionStatus());

            return response;
        } catch (HttpResponseException e) {
            handleHttpResponseException(e);
            throw e;
        }
    }

    public GetPaymentDetails getPaymentDetails(URL url) {
        final String requestId = UUID.randomUUID().toString();

        return createRequestInSession(url, StorageKeys.INITIAL_TOKEN)
                .header(HeaderKeys.X_API_KEY, configuration.getApiKey())
                .header(HeaderKeys.X_REQUEST_ID, requestId)
                .header(HeaderKeys.PSU_IP_ADDRESS, HeaderValues.PSU_IP_ADDRESS)
                .get(GetPaymentDetails.class);
    }

    public FetchDomesticPaymentResponse fetchDomesticPayment(String paymentId) {
        final String requestId = UUID.randomUUID().toString();
        URL url =
                new URL(configuration.getBaseUrl() + Endpoints.FETCH_DOMESTIC_PAYMENT)
                        .parameter(IdTags.PAYMENT_ID, paymentId);

        return createRequestInSession(url, StorageKeys.INITIAL_TOKEN)
                .header(HeaderKeys.X_API_KEY, configuration.getApiKey())
                .header(HeaderKeys.X_REQUEST_ID, requestId)
                .header(HeaderKeys.PSU_IP_ADDRESS, HeaderValues.PSU_IP_ADDRESS)
                .get(FetchDomesticPaymentResponse.class);
    }

    public FetchCrossBorderPaymentResponse fetchCrossBorderPayment(String paymentId) {
        final String requestId = UUID.randomUUID().toString();
        URL url =
                new URL(configuration.getBaseUrl() + Endpoints.FETCH_CROSS_BORDER_PAYMENT)
                        .parameter(IdTags.PAYMENT_ID, paymentId);

        return createRequestInSession(url, StorageKeys.INITIAL_TOKEN)
                .header(HeaderKeys.X_API_KEY, configuration.getApiKey())
                .header(HeaderKeys.X_REQUEST_ID, requestId)
                .header(HeaderKeys.PSU_IP_ADDRESS, HeaderValues.PSU_IP_ADDRESS)
                .get(FetchCrossBorderPaymentResponse.class);
    }

    public FetchSepaPaymentResponse fetchSepaPayment(String paymentId) {
        final String requestId = UUID.randomUUID().toString();
        URL url =
                new URL(configuration.getBaseUrl() + Endpoints.FETCH_SEPA_PAYMENT)
                        .parameter(IdTags.PAYMENT_ID, paymentId);

        return createRequestInSession(url, StorageKeys.INITIAL_TOKEN)
                .header(HeaderKeys.X_API_KEY, configuration.getApiKey())
                .header(HeaderKeys.X_REQUEST_ID, requestId)
                .header(HeaderKeys.PSU_IP_ADDRESS, HeaderValues.PSU_IP_ADDRESS)
                .get(FetchSepaPaymentResponse.class);
    }

    private AccountEntity fetchBalances(final AccountEntity accountEntity) {
        final String requestId = UUID.randomUUID().toString();
        final URL url =
                new URL(
                        getConfiguration().getBaseUrl()
                                + Endpoints.AIS_PRODUCT
                                + accountEntity.getBalancesLink());

        final List<BalanceEntity> balances =
                client.request(url)
                        .addBearerToken(getTokenFromSession(StorageKeys.OAUTH_TOKEN))
                        .header(HeaderKeys.CONSENT_ID, sessionStorage.get(StorageKeys.CONSENT_ID))
                        .header(HeaderKeys.X_REQUEST_ID, requestId)
                        .type(MediaType.APPLICATION_JSON)
                        .header(HeaderKeys.X_API_KEY, configuration.getApiKey())
                        .get(AccountEntity.class)
                        .getBalances();
        accountEntity.setBalances(balances);

        return accountEntity;
    }

    public AccountResponse fetchAccounts() {
        final String requestId = UUID.randomUUID().toString();
        URL url = new URL(configuration.getBaseUrl() + Endpoints.ACCOUNTS);

        final AccountResponse accountsWithoutBalances =
                createRequestInSession(url, StorageKeys.OAUTH_TOKEN)
                        .queryParam(QueryKeys.WITH_BALANCE, QueryValues.TRUE)
                        .header(HeaderKeys.CONSENT_ID, sessionStorage.get(StorageKeys.CONSENT_ID))
                        .header(HeaderKeys.X_REQUEST_ID, requestId)
                        .type(MediaType.APPLICATION_JSON)
                        .header(HeaderKeys.X_API_KEY, configuration.getApiKey())
                        .get(AccountResponse.class);

        final List<AccountEntity> accountsWithBalances =
                accountsWithoutBalances.getAccounts().stream()
                        .map(this::fetchBalances)
                        .collect(Collectors.toList());

        return new AccountResponse(accountsWithBalances);
    }

    public TransactionResponse fetchTransactions(
            TransactionalAccount account, Date fromDate, Date toDate) {
        final String requestId = UUID.randomUUID().toString();
        final URL fullUrl =
                new URL(
                        configuration.getBaseUrl()
                                + Endpoints.AIS_PRODUCT
                                + account.getFromTemporaryStorage(StorageKeys.TRANSACTIONS_URL));

        return createRequestInSession(fullUrl, StorageKeys.OAUTH_TOKEN)
                .header(HeaderKeys.X_REQUEST_ID, requestId)
                .header(HeaderKeys.X_API_KEY, configuration.getApiKey())
                .header(HeaderKeys.CONSENT_ID, sessionStorage.get(StorageKeys.CONSENT_ID))
                .queryParam(
                        QueryKeys.DATE_TO,
                        DateUtils.formatDateTime(toDate, Format.TIMESTAMP, Format.TIMEZONE))
                .queryParam(
                        QueryKeys.DATE_FROM,
                        DateUtils.formatDateTime(fromDate, Format.TIMESTAMP, Format.TIMEZONE))
                .get(TransactionResponse.class);
    }

    public URL getAuthorizeUrl(String state, String consentId) {
        authorizeConsent(consentId);
        final String clientId = getConfiguration().getClientId();
        final String redirectUri = getConfiguration().getRedirectUrl();
        final String codeVerifier = BerlinGroupUtils.generateCodeVerifier();
        final String codeChallenge = BerlinGroupUtils.generateCodeChallenge(codeVerifier);
        sessionStorage.put(StorageKeys.CODE_VERIFIER, codeVerifier);
        sessionStorage.put(StorageKeys.CONSENT_ID, consentId);
        URL url = new URL(configuration.getBaseAuthUrl() + Endpoints.AUTHORIZE);

        return createRequest(url)
                .queryParam(QueryKeys.RESPONSE_TYPE, BankdataConstants.QueryValues.CODE)
                .queryParam(QueryKeys.CLIENT_ID, clientId)
                .queryParam(QueryKeys.SCOPE, QueryValues.SCOPE + consentId)
                .queryParam(QueryKeys.STATE, state)
                .queryParam(QueryKeys.CODE_CHALLENGE_METHOD, QueryValues.CODE_CHALLENGE_METHOD)
                .queryParam(QueryKeys.CODE_CHALLENGE, codeChallenge)
                .queryParam(QueryKeys.REDIRECT_URI, redirectUri)
                .getUrl();
    }

    public void authorizePayment(String paymentId, PaymentType type) {
        final String requestId = UUID.randomUUID().toString();
        AuthorizePaymentRequest request = new AuthorizePaymentRequest();

        String productType = BankdataConstants.TYPE_TO_DOMAIN_MAPPER.get(type);

        URL url =
                new URL(configuration.getBaseUrl() + productType + Endpoints.AUTHORIZE_PAYMENT)
                        .parameter(IdTags.PAYMENT_ID, paymentId);

        createRequestInSession(url, StorageKeys.INITIAL_TOKEN)
                .header(HeaderKeys.X_API_KEY, configuration.getApiKey())
                .header(HeaderKeys.X_REQUEST_ID, requestId)
                .header(HeaderKeys.PSU_IP_ADDRESS, HeaderValues.PSU_IP_ADDRESS)
                .post(AuthorizePaymentResponse.class, request);
    }

    public void signPayment(String paymentId) {
        final String codeVerifier = BerlinGroupUtils.generateCodeVerifier();
        final String codeChallenge = BerlinGroupUtils.generateCodeChallenge(codeVerifier);
        final String clientId = getConfiguration().getClientId();
        final String redirectUri = getConfiguration().getRedirectUrl();
        sessionStorage.put(StorageKeys.CONSENT_ID, paymentId);
        sessionStorage.put(StorageKeys.CODE_VERIFIER, codeVerifier);
        URL url =
                new URL(configuration.getBaseAuthUrl() + Endpoints.AUTHORIZE)
                        .queryParam(QueryKeys.RESPONSE_TYPE, BankdataConstants.QueryValues.CODE)
                        .queryParam(QueryKeys.CLIENT_ID, clientId)
                        .queryParam(QueryKeys.SCOPE, QueryValues.PIS_SCOPE + paymentId)
                        .queryParam(QueryKeys.STATE, this.state)
                        .queryParam(
                                QueryKeys.CODE_CHALLENGE_METHOD, QueryValues.CODE_CHALLENGE_METHOD)
                        .queryParam(QueryKeys.CODE_CHALLENGE, codeChallenge)
                        .queryParam(QueryKeys.REDIRECT_URI, redirectUri);

        openThirdPartyApp(url);
    }

    private void openThirdPartyApp(URL authorizeUrl) {
        ThirdPartyAppAuthenticationPayload payload = this.getAppPayload(authorizeUrl);
        Preconditions.checkNotNull(payload);
        this.supplementalInformationHelper.openThirdPartyApp(payload);
        this.collect();
    }

    public ThirdPartyAppAuthenticationPayload getAppPayload(URL authorizeUrl) {
        ThirdPartyAppAuthenticationPayload payload = new ThirdPartyAppAuthenticationPayload();

        Android androidPayload = new Android();
        androidPayload.setIntent(authorizeUrl.get());

        Ios iOsPayload = new Ios();
        iOsPayload.setDeepLinkUrl(authorizeUrl.get());
        iOsPayload.setAppScheme(authorizeUrl.getScheme());

        payload.setAndroid(androidPayload);
        payload.setIos(iOsPayload);
        return payload;
    }

    public ThirdPartyAppResponse<String> collect() {

        Optional<Map<String, String>> optionalCallbackData =
                this.supplementalInformationHelper.waitForSupplementalInformation(
                        OAuthUtils.formatSupplementalKey(this.state),
                        WAIT_FOR_SECONDS,
                        TimeUnit.SECONDS);

        String code =
                optionalCallbackData
                        .map(stringStringMap -> stringStringMap.getOrDefault("code", null))
                        .orElseThrow(() -> new IllegalStateException("No code in api response"));

        getToken(code);

        return ThirdPartyAppResponseImpl.create(ThirdPartyAppStatus.DONE);
    }

    public void getTokenWithClientCredentials() {
        final InitialTokenRequest request =
                new InitialTokenRequest(
                        FormValues.CLIENT_CREDENTIALS,
                        FormValues.SCOPE,
                        getConfiguration().getClientId());
        URL url = new URL(configuration.getBaseAuthUrl() + Endpoints.TOKEN);

        TokenResponse initialToken =
                client.request(url)
                        .type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                        .post(TokenResponse.class, request.toData());

        sessionStorage.put(StorageKeys.INITIAL_TOKEN, initialToken.toTinkToken());
    }

    public String getConsentId() {
        final ConsentRequest consentRequest = new ConsentRequest();
        final String requestId = UUID.randomUUID().toString();
        URL url = new URL(configuration.getBaseUrl() + Endpoints.CONSENT);

        return client.request(url)
                .addBearerToken(getTokenFromSession(StorageKeys.INITIAL_TOKEN))
                .header(HeaderKeys.X_API_KEY, getConfiguration().getApiKey())
                .header(HeaderKeys.X_REQUEST_ID, requestId)
                .body(consentRequest.toData(), MediaType.APPLICATION_JSON_TYPE)
                .post(ConsentResponse.class)
                .getConsentId();
    }

    public void authorizeConsent(String consentId) {
        final String requestId = UUID.randomUUID().toString();
        final ConsentAuthorizationRequest consentAuthorization = new ConsentAuthorizationRequest();
        URL url = new URL(configuration.getBaseUrl() + Endpoints.AUTHORIZE_CONSENT);

        client.request(url.parameter(IdTags.CONSENT_ID, consentId))
                .addBearerToken(getTokenFromSession(StorageKeys.INITIAL_TOKEN))
                .header(HeaderKeys.X_API_KEY, getConfiguration().getApiKey())
                .header(HeaderKeys.X_REQUEST_ID, requestId)
                .body(consentAuthorization, MediaType.APPLICATION_JSON_TYPE)
                .post(ConsentAuthorizationResponse.class);
    }

    public OAuth2Token getToken(String code) {
        final AccessTokenRequest request =
                new AccessTokenRequest(
                        BankdataConstants.FormValues.AUTHORIZATION_CODE,
                        getConfiguration().getClientId(),
                        code,
                        getConfiguration().getRedirectUrl(),
                        sessionStorage.get(StorageKeys.CODE_VERIFIER));

        return getTokenResponse(request);
    }

    private OAuth2Token getTokenFromSession(String storageKey) {
        return sessionStorage
                .get(storageKey, OAuth2Token.class)
                .orElseThrow(
                        () -> new IllegalStateException(SessionError.SESSION_EXPIRED.exception()));
    }

    public void setTokenToSession(OAuth2Token accessToken, String storageKey) {
        sessionStorage.put(storageKey, accessToken);
    }

    public OAuth2Token refreshToken(String refreshToken) {
        final RefreshTokenRequest request =
                new RefreshTokenRequest(
                        FormValues.REFRESH_TOKEN_GRANT_TYPE,
                        refreshToken,
                        getConfiguration().getRedirectUrl(),
                        getConfiguration().getClientId(),
                        sessionStorage.get(StorageKeys.CODE_VERIFIER));

        return getTokenResponse(request);
    }

    private OAuth2Token getTokenResponse(TokenRequest tokenRequest) {
        URL url = new URL(configuration.getBaseAuthUrl() + Endpoints.TOKEN);

        return client.request(url)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                .post(TokenResponse.class, tokenRequest.toData())
                .toTinkToken();
    }

    private void handleHttpResponseException(HttpResponseException httpResponseException)
            throws PaymentException {
        if (httpResponseException.getResponse().hasBody()) {
            try {
                httpResponseException
                        .getResponse()
                        .getBody(BankdataErrorResponse.class)
                        .checkError(httpResponseException);
            } catch (HttpClientException | HttpResponseException d) {
                throw httpResponseException;
            }
        }
    }
}
