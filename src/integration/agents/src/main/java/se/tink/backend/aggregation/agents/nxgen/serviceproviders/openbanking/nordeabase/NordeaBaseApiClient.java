package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseConstants.QueryValues.PAYMENT_SCOPE;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.consent.generators.serviceproviders.nordea.NordeaConsentGenerator;
import se.tink.backend.aggregation.agents.consent.generators.serviceproviders.nordea.NordeaScope;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyError;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseConstants.ApiService;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseConstants.BodyValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseConstants.ErrorCodes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseConstants.HeaderValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseConstants.IdTags;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseConstants.Scopes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseConstants.Signature;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.authenticator.rpc.AuthorizeRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.authenticator.rpc.AuthorizeRequest.AuthorizeRequestBuilder;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.authenticator.rpc.GetTokenForm;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.authenticator.rpc.GetTokenResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.authenticator.rpc.RefreshTokenForm;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.authenticator.rpc.UserAssetsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.configuration.NordeaBaseConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.executor.payment.rpc.CancelPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.executor.payment.rpc.ConfirmPaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.executor.payment.rpc.ConfirmPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.executor.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.executor.payment.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.executor.payment.rpc.CreateSingleScaPaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.executor.payment.rpc.CreateSingleScaPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.executor.payment.rpc.GetPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.executor.payment.rpc.GetPaymentsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.executor.payment.rpc.SingleScaPaymentStatusResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.fetcher.creditcard.rpc.CreditCardResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.fetcher.transactionalaccount.entities.AccountDetailsResponseEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.fetcher.transactionalaccount.rpc.GetAccountDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.fetcher.transactionalaccount.rpc.GetAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.fetcher.transactionalaccount.rpc.GetTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.rpc.NordeaErrorResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.util.SignatureUtil;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants.PersistentStorageKeys;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.credentials.service.UserAvailability;
import se.tink.libraries.payment.enums.PaymentType;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class NordeaBaseApiClient implements TokenInterface {
    protected final AgentComponentProvider componentProvider;
    protected final TinkHttpClient client;
    protected final PersistentStorage persistentStorage;
    protected NordeaBaseConfiguration configuration;
    protected String redirectUrl;
    private final QsealcSigner qsealcSigner;
    private final boolean corporate;
    private GetAccountsResponse cachedAccounts;
    private final UserAvailability userAvailability;
    private final LocalDate localDate;
    private final StrongAuthenticationState strongAuthenticationState;

    public NordeaBaseApiClient(
            AgentComponentProvider componentProvider,
            TinkHttpClient client,
            PersistentStorage persistentStorage,
            QsealcSigner qsealcSigner,
            boolean corporate,
            StrongAuthenticationState strongAuthenticationState) {
        this.componentProvider = componentProvider;
        this.client = client;
        this.persistentStorage = persistentStorage;
        this.qsealcSigner = qsealcSigner;
        this.corporate = corporate;
        this.userAvailability = componentProvider.getCredentialsRequest().getUserAvailability();
        this.localDate = componentProvider.getLocalDateTimeSource().now().toLocalDate();
        this.strongAuthenticationState = strongAuthenticationState;
    }

    public NordeaBaseConfiguration getConfiguration() {
        return Optional.ofNullable(configuration)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    public String getRedirectUrl() {
        return Optional.ofNullable(redirectUrl)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    public void setConfiguration(AgentConfiguration<NordeaBaseConfiguration> agentConfiguration) {
        this.configuration = agentConfiguration.getProviderSpecificConfiguration();
        this.redirectUrl = agentConfiguration.getRedirectUrl();
    }

    protected RequestBuilder createRequest(URL url, String httpMethod, String body) {
        String digest = null;
        if (!Strings.isNullOrEmpty(body)) {
            digest = SignatureUtil.generateDigest(body);
        }

        String date = getServerDate();
        RequestBuilder builder =
                client.request(url)
                        .type(MediaType.APPLICATION_JSON)
                        .header(HeaderKeys.X_CLIENT_ID, configuration.getClientId())
                        .header(HeaderKeys.X_CLIENT_SECRET, configuration.getClientSecret())
                        .header(HeaderKeys.ORIGINATING_HOST, HeaderValues.HOST)
                        .header(HeaderKeys.ORIGINATING_DATE, date)
                        .header(
                                HeaderKeys.SIGNATURE,
                                createSignature(url, httpMethod, digest, date));

        if (userAvailability.isUserPresent()) {
            builder.header(HeaderKeys.USER_IP, userAvailability.getOriginatingUserIpOrDefault());
        }

        if (!Strings.isNullOrEmpty(body)) {
            builder.header(HeaderKeys.DIGEST, digest);
        }

        return builder;
    }

    private RequestBuilder createRequestInSession(URL url, String httpMethod, String body) {
        OAuth2Token token = getStoredToken();
        return createRequest(url, httpMethod, body)
                .header(
                        HeaderKeys.AUTHORIZATION,
                        token.getTokenType() + " " + token.getAccessToken());
    }

    private RequestBuilder createTokenRequest(String body) {
        return createRequest(Urls.GET_TOKEN, HttpMethod.POST, body);
    }

    public URL getAuthorizeUrl(AuthorizeRequestBuilder builder) {

        AuthorizeRequest authorizeRequest =
                builder.withRedirectUri(getRedirectUrl())
                        .withScope(getScopes())
                        .withDuration(BodyValues.DURATION_MINUTES)
                        .withMaxTransactionHistory(BodyValues.FETCH_NUMBER_OF_MONTHS)
                        .withAccountList(setAccountList(corporate))
                        .build();
        String requestBody = SerializationUtils.serializeToString(authorizeRequest);

        try {
            createRequest(setAuthorizeUrl(corporate), HttpMethod.POST, requestBody)
                    .post(requestBody);
        } catch (HttpResponseException e) {
            if (e.getResponse().getStatus() == HttpStatus.SC_MOVED_TEMPORARILY) {
                return URL.of(e.getResponse().getHeaders().get(HeaderKeys.LOCATION).get(0));
            }
            handleHttpAisResponseException(e);
        }
        throw LoginError.DEFAULT_MESSAGE.exception();
    }

    private URL setAuthorizeUrl(boolean corporate) {
        return corporate ? Urls.AUTHORIZE_BUSINESS : Urls.AUTHORIZE;
    }

    private String setAccountList(boolean corporate) {
        return corporate ? BodyValues.ALL : BodyValues.ALL_WITH_CARDS;
    }

    private void handleHttpAisResponseException(HttpResponseException e) {
        if (e.getResponse().getStatus() == HttpStatus.SC_UNAUTHORIZED
                && e.getResponse().getBody(String.class).contains("Invalid client id or secret")) {
            throw ThirdPartyError.INCORRECT_SECRETS.exception();
        }
        throw e;
    }

    public OAuth2Token getToken(GetTokenForm form) {
        String body = form.getBodyValue();
        return createTokenRequest(body)
                .body(body, MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                .post(GetTokenResponse.class)
                .toTinkToken();
    }

    public OAuth2Token refreshToken(String refreshToken) {
        String body = RefreshTokenForm.of(refreshToken).getBodyValue();
        return createTokenRequest(body)
                .body(body, MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                .post(GetTokenResponse.class)
                .toTinkToken();
    }

    public UserAssetsResponse fetchUserAssets() {
        return requestRefreshableGet(
                createRequestInSession(Urls.GET_ASSETS, HttpMethod.GET, null),
                UserAssetsResponse.class);
    }

    public GetAccountsResponse getAccounts() {
        if (cachedAccounts == null) {
            cachedAccounts =
                    requestRefreshableGet(
                            createRequestInSession(
                                    corporate ? Urls.GET_BUSINESS_ACCOUNTS : Urls.GET_ACCOUNTS,
                                    HttpMethod.GET,
                                    null),
                            GetAccountsResponse.class);
        }
        return cachedAccounts;
    }

    public AccountDetailsResponseEntity getAccountDetails(String id) {
        return createRequestInSession(
                        new URL(Urls.GET_BUSINESS_ACCOUNT_DETAILS + id), HttpMethod.GET, null)
                .get(GetAccountDetailsResponse.class)
                .getResponse();
    }

    public GetTransactionsResponse getCorporateTransactions(
            TransactionalAccount account, String key) {
        URL url =
                Optional.ofNullable(key)
                        .map(k -> new URL(Urls.BASE_BUSINESS_URL + k))
                        .orElse(
                                Urls.GET_BUSINESS_TRANSACTIONS.parameter(
                                        IdTags.ACCOUNT_ID, account.getApiIdentifier()));

        return createRequestInSession(url, HttpMethod.GET, null).get(GetTransactionsResponse.class);
    }

    private URL getUrlWithKey(String key) {
        return new URL((corporate ? Urls.BASE_BUSINESS_URL : Urls.BASE_URL) + key);
    }

    private URL getTransactionsUrl(String accountId) {
        return (corporate ? Urls.GET_BUSINESS_TRANSACTIONS : Urls.GET_TRANSACTIONS)
                .parameter(IdTags.ACCOUNT_ID, accountId)
                .queryParam(
                        QueryKeys.FROM_DATE,
                        localDate.minusDays(QueryValues.FETCH_NUMBER_OF_DAYS).toString())
                .queryParam(QueryKeys.TO_DATE, localDate.toString());
    }

    public <T> T getTransactions(TransactionalAccount account, String key, Class<T> responseClass) {
        URL url =
                Optional.ofNullable(key)
                        .map(this::getUrlWithKey)
                        .orElse(getTransactionsUrl(account.getApiIdentifier()));

        RequestBuilder request = createRequestInSession(url, HttpMethod.GET, null);

        return requestRefreshableGet(request, responseClass);
    }

    public CreditCardResponse fetchCreditCards() {
        return requestRefreshableGet(
                createRequestInSession(Urls.GET_CARDS, HttpMethod.GET, null),
                CreditCardResponse.class);
    }

    public CreditCardResponse fetchCreditCardDetails(String cardId) {
        return requestRefreshableGet(
                createRequestInSession(
                        Urls.GET_CARD_DETAILS.parameter(NordeaBaseConstants.IdTags.CARD_ID, cardId),
                        HttpMethod.GET,
                        null),
                CreditCardResponse.class);
    }

    public <T> T fetchCreditCardTransactions(
            CreditCardAccount account, String page, Class<T> responseClass) {
        return requestRefreshableGet(
                createRequestInSession(
                        Urls.GET_CARD_TRANSACTIONS
                                .parameter(
                                        NordeaBaseConstants.IdTags.CARD_ID,
                                        account.getApiIdentifier())
                                .queryParam(NordeaBaseConstants.QueryKeys.CONTINUATION_KEY, page),
                        HttpMethod.GET,
                        null),
                responseClass);
    }

    @Override
    public void storeToken(OAuth2Token token) {
        persistentStorage.rotateStorageValue(PersistentStorageKeys.OAUTH_2_TOKEN, token);
    }

    @Override
    public OAuth2Token getStoredToken() {
        return persistentStorage
                .get(PersistentStorageKeys.OAUTH_2_TOKEN, OAuth2Token.class)
                .orElseThrow(() -> new IllegalStateException("Cannot find token!"));
    }

    public CreatePaymentResponse createPayment(
            CreatePaymentRequest createPaymentRequest, PaymentType paymentType)
            throws PaymentException {
        String body = SerializationUtils.serializeToString(createPaymentRequest);
        try {
            return createRequestInSession(
                            Urls.INITIATE_PAYMENT.parameter(
                                    IdTags.PAYMENT_TYPE, paymentType.toString()),
                            HttpMethod.POST,
                            body)
                    .post(CreatePaymentResponse.class, createPaymentRequest);
        } catch (HttpResponseException e) {
            handleHttpPisResponseException(e);
            throw e;
        }
    }

    public ConfirmPaymentResponse confirmPayment(
            ConfirmPaymentRequest confirmPaymentRequest, PaymentType paymentType)
            throws PaymentException {
        try {
            String requestBody = SerializationUtils.serializeToString(confirmPaymentRequest);
            return createRequestInSession(
                            Urls.CONFIRM_PAYMENT.parameter(
                                    IdTags.PAYMENT_TYPE, paymentType.toString()),
                            HttpMethod.PUT,
                            requestBody)
                    .put(ConfirmPaymentResponse.class, confirmPaymentRequest);
        } catch (HttpResponseException e) {
            handleHttpPisResponseException(e);
            throw e;
        }
    }

    public CreateSingleScaPaymentResponse createSingleScaPayment(
            CreatePaymentRequest createPaymentRequest) throws PaymentException {
        CreateSingleScaPaymentRequest createSingleScaPaymentRequest =
                CreateSingleScaPaymentRequest.builder()
                        .withPayment(createPaymentRequest)
                        .withExternalId(createPaymentRequest.getExternalId())
                        .withRedirectUri(getRedirectUrl())
                        .withState(strongAuthenticationState.getState())
                        .build();
        String body = SerializationUtils.serializeToString(createSingleScaPaymentRequest);
        try {
            return createRequest(Urls.INITIATE_SINGLE_SCA_PAYMENT, HttpMethod.POST, body)
                    .post(CreateSingleScaPaymentResponse.class, createSingleScaPaymentRequest);
        } catch (HttpResponseException e) {
            handleHttpPisResponseException(e);
            throw e;
        }
    }

    public ConfirmPaymentResponse confirmPaymentList(List<String> paymentIds)
            throws PaymentException {
        ConfirmPaymentRequest confirmPaymentRequest =
                new ConfirmPaymentRequest(paymentIds, strongAuthenticationState.getState());
        String body = SerializationUtils.serializeToString(confirmPaymentRequest);
        try {
            return createRequestInSession(Urls.CONFIRM_PAYMENT_LIST, HttpMethod.PUT, body)
                    .put(ConfirmPaymentResponse.class, confirmPaymentRequest);
        } catch (HttpResponseException e) {
            handleHttpPisResponseException(e);
            throw e;
        }
    }

    public GetPaymentResponse getPayment(String paymentId, PaymentType paymentType)
            throws PaymentException {
        try {
            return createRequestInSession(
                            Urls.GET_PAYMENT
                                    .parameter(IdTags.PAYMENT_TYPE, paymentType.toString())
                                    .parameter(IdTags.PAYMENT_ID, paymentId),
                            HttpMethod.GET,
                            null)
                    .get(GetPaymentResponse.class);
        } catch (HttpResponseException e) {
            handleHttpPisResponseException(e);
            throw e;
        }
    }

    public SingleScaPaymentStatusResponse getSingleScaPayment(String paymentId)
            throws PaymentException {
        try {
            return createRequest(
                            Urls.GET_SINGLE_SCA_PAYMENT.parameter(IdTags.PAYMENT_ID, paymentId),
                            HttpMethod.GET,
                            null)
                    .get(SingleScaPaymentStatusResponse.class);
        } catch (HttpResponseException e) {
            handleHttpPisResponseException(e);
            throw e;
        }
    }

    public GetPaymentsResponse fetchPayments(PaymentType paymentType) throws PaymentException {
        try {
            return createRequestInSession(
                            Urls.GET_PAYMENTS.parameter(
                                    IdTags.PAYMENT_TYPE, paymentType.toString()),
                            HttpMethod.GET,
                            null)
                    .get(GetPaymentsResponse.class);
        } catch (HttpResponseException e) {
            handleHttpPisResponseException(e);
            throw e;
        }
    }

    private void handleHttpPisResponseException(HttpResponseException httpResponseException)
            throws PaymentException {
        if (httpResponseException.getResponse().hasBody()) {
            try {
                httpResponseException
                        .getResponse()
                        .getBody(NordeaErrorResponse.class)
                        .checkPisError(httpResponseException);
            } catch (HttpClientException | HttpResponseException d) {
                throw httpResponseException;
            }
        }
    }

    protected <T> T requestRefreshableGet(RequestBuilder request, Class<T> responseType) {
        try {
            return request.get(responseType);

        } catch (HttpResponseException hre) {

            validateIsAccessTokenEpiredOrElseThrow(hre);

            OAuth2Token oAuth2Token =
                    this.refreshToken(
                            getStoredToken()
                                    .getRefreshToken()
                                    .orElseThrow(
                                            () ->
                                                    new IllegalStateException(
                                                            "Could not find refresh token to refresh.",
                                                            hre)));
            this.storeToken(oAuth2Token);

            request.overrideHeader(HttpHeaders.AUTHORIZATION, oAuth2Token.toAuthorizeHeader());
            // retry request with new token
            return requestRefreshableGet(request, responseType);
        }
    }

    private void validateIsAccessTokenEpiredOrElseThrow(HttpResponseException hre) {
        HttpResponse response = hre.getResponse();
        if (response.getStatus() == HttpStatus.SC_UNAUTHORIZED && response.hasBody()) {
            String body = response.getBody(String.class);
            if (!Strings.isNullOrEmpty(body)
                    && body.toLowerCase().contains(ErrorMessages.TOKEN_EXPIRED.toLowerCase())
                    && body.toLowerCase().contains(ErrorCodes.TOKEN_EXPIRED.toLowerCase())) {
                return;
            }
        }
        throw hre;
    }

    protected Set<String> getScopes() {
        List<String> integrationScopes = configuration.getScopes();
        Set<String> generatedScopes =
                new NordeaConsentGenerator(componentProvider, Sets.newHashSet(NordeaScope.values()))
                        .generate();

        // TODO investigate and fix below scopes
        if (isOnlyForAis(integrationScopes)) {
            return Sets.newHashSet(
                    "ACCOUNTS_BALANCES",
                    "ACCOUNTS_BASIC",
                    "ACCOUNTS_DETAILS",
                    "ACCOUNTS_TRANSACTIONS",
                    "CARDS_INFORMATION",
                    "CARDS_TRANSACTIONS");
        } else if (isForAisAndPis(integrationScopes)) {
            generatedScopes.add(PAYMENT_SCOPE);
            return Sets.newHashSet(
                    "ACCOUNTS_BALANCES",
                    "ACCOUNTS_BASIC",
                    "ACCOUNTS_DETAILS",
                    "ACCOUNTS_TRANSACTIONS",
                    "PAYMENTS_MULTIPLE",
                    "CARDS_INFORMATION",
                    "CARDS_TRANSACTIONS");
        } else {
            throw new IllegalArgumentException(
                    String.format(
                            "%s contain invalid scope(s), only support scopes AIS and PIS",
                            integrationScopes.toString()));
        }
    }

    private boolean isOnlyForAis(List<String> integrationScopes) {
        return integrationScopes.stream().allMatch(Scopes.AIS::equalsIgnoreCase);
    }

    private boolean isForAisAndPis(List<String> integrationScopes) {
        return integrationScopes.stream()
                .allMatch(
                        scope ->
                                Scopes.AIS.equalsIgnoreCase(scope)
                                        || Scopes.PIS.equalsIgnoreCase(scope));
    }

    private String createSignature(
            URL requestUrl, String httpMethod, final String body, String date) {
        String contentType = MediaType.APPLICATION_JSON;
        if (requestUrl.toString().contains(ApiService.GET_TOKEN_DECOUPLED)) {
            contentType = MediaType.APPLICATION_FORM_URLENCODED;
        }

        if (!Strings.isNullOrEmpty(body)) {
            return SignatureUtil.createPostSignature(
                    getConfiguration().getClientId(),
                    httpMethod,
                    requestUrl.toUri(),
                    date,
                    body,
                    contentType,
                    qsealcSigner);
        } else {
            return SignatureUtil.createGetSignature(
                    getConfiguration().getClientId(),
                    httpMethod,
                    requestUrl.toUri(),
                    date,
                    qsealcSigner);
        }
    }

    private String getServerDate() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat(Signature.DATE_FORMAT, Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone(Signature.TIMEZONE));
        return dateFormat.format(calendar.getTime());
    }

    public CancelPaymentResponse deletePayment(String paymentId, PaymentType paymentType) {

        return createRequestInSession(
                        Urls.DELETE_PAYMENT
                                .parameter(IdTags.PAYMENT_TYPE, paymentType.toString())
                                .parameter(IdTags.PAYMENT_ID, paymentId),
                        HttpMethod.DELETE,
                        null)
                .delete(CancelPaymentResponse.class);
    }
}
