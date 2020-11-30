package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.TimeZone;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseConstants.ApiService;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseConstants.BodyValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseConstants.ErrorCodes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseConstants.HeaderValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseConstants.IdTags;
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
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.executor.payment.rpc.ConfirmPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.executor.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.executor.payment.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.executor.payment.rpc.GetPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.executor.payment.rpc.GetPaymentsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.fetcher.creditcard.rpc.CreditCardResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.fetcher.creditcard.rpc.CreditCardTransactionResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.fetcher.transactionalaccount.rpc.GetAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.fetcher.transactionalaccount.rpc.GetTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.filters.BankSideFailureFilter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.filters.BankSideRetryFilter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.rpc.NordeaErrorResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.util.SignatureUtil;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants.PersistentStorageKeys;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.filter.filters.AccessExceededFilter;
import se.tink.backend.aggregation.nxgen.http.filter.filters.BadGatewayFilter;
import se.tink.backend.aggregation.nxgen.http.filter.filters.ServiceUnavailableBankServiceErrorFilter;
import se.tink.backend.aggregation.nxgen.http.filter.filters.TimeoutFilter;
import se.tink.backend.aggregation.nxgen.http.filter.filters.randomretry.RateLimitRetryFilter;
import se.tink.backend.aggregation.nxgen.http.filter.filters.retry.BadGatewayRetryFilter;
import se.tink.backend.aggregation.nxgen.http.filter.filters.retry.TimeoutRetryFilter;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.payment.enums.PaymentType;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class NordeaBaseApiClient implements TokenInterface {
    protected final TinkHttpClient client;
    protected final PersistentStorage persistentStorage;
    protected NordeaBaseConfiguration configuration;
    protected String redirectUrl;
    private final QsealcSigner qsealcSigner;
    private final boolean corporate;
    private GetAccountsResponse cachedAccounts;

    public NordeaBaseApiClient(
            TinkHttpClient client,
            PersistentStorage persistentStorage,
            QsealcSigner qsealcSigner,
            String providerName,
            boolean corporate) {
        this.client = client;
        this.persistentStorage = persistentStorage;
        this.qsealcSigner = qsealcSigner;
        this.corporate = corporate;
        configureFilters(providerName);
    }

    private void configureFilters(String providerName) {
        this.client.addFilter(new BankSideFailureFilter());
        this.client.addFilter(new BankSideRetryFilter());
        this.client.addFilter(new ServiceUnavailableBankServiceErrorFilter());
        this.client.addFilter(new TimeoutFilter());
        this.client.addFilter(
                new TimeoutRetryFilter(
                        NordeaBaseConstants.Filters.NUMBER_OF_RETRIES,
                        NordeaBaseConstants.Filters.MS_TO_WAIT));
        this.client.addFilter(new BadGatewayFilter());
        this.client.addFilter(
                new BadGatewayRetryFilter(
                        NordeaBaseConstants.Filters.NUMBER_OF_RETRIES,
                        NordeaBaseConstants.Filters.MS_TO_WAIT));
        this.client.addFilter(new AccessExceededFilter(providerName));
        this.client.addFilter(
                new RateLimitRetryFilter(
                        NordeaBaseConstants.Filters.NUMBER_OF_RETRIES,
                        NordeaBaseConstants.Filters.MS_TO_WAIT));
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
                        .withScope(ImmutableList.copyOf(getScopes().split(",")))
                        .withDuration(BodyValues.DURATION_MINUTES)
                        .withMaxTransactionHistory(BodyValues.FETCH_NUMBER_OF_MONTHS)
                        .build();
        String requestBody = SerializationUtils.serializeToString(authorizeRequest);
        try {
            createRequest(Urls.AUTHORIZE, HttpMethod.POST, requestBody).post(requestBody);
        } catch (HttpResponseException e) {
            if (e.getResponse().getStatus() == HttpStatus.SC_MOVED_TEMPORARILY) {
                return URL.of(e.getResponse().getHeaders().get(HeaderKeys.LOCATION).get(0));
            } else {
                throw e;
            }
        }
        throw LoginError.DEFAULT_MESSAGE.exception();
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
                                    corporate ? Urls.GET_CORPORATE_ACCOUNTS : Urls.GET_ACCOUNTS,
                                    HttpMethod.GET,
                                    null),
                            GetAccountsResponse.class);
        }
        return cachedAccounts;
    }

    public GetTransactionsResponse getCorporateTransactions(
            TransactionalAccount account, String key) {
        URL url =
                Optional.ofNullable(key)
                        .map(k -> new URL(Urls.BASE_CORPORATE_URL + k))
                        .orElse(
                                Urls.GET_CORPORATE_TRANSACTIONS.parameter(
                                        IdTags.ACCOUNT_ID, account.getApiIdentifier()));

        return createRequestInSession(url, HttpMethod.GET, null).get(GetTransactionsResponse.class);
    }

    private URL getUrlWithKey(String key) {
        return new URL((corporate ? Urls.BASE_CORPORATE_URL : Urls.BASE_URL) + key);
    }

    private URL getTransactionsUrl(String accountId) {
        return (corporate ? Urls.GET_CORPORATE_TRANSACTIONS : Urls.GET_TRANSACTIONS)
                .parameter(IdTags.ACCOUNT_ID, accountId);
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

    public CreditCardTransactionResponse fetchCreditCardTransactions(
            CreditCardAccount account, String page) {
        return requestRefreshableGet(
                createRequestInSession(
                        Urls.GET_CARD_TRANSACTIONS
                                .parameter(
                                        NordeaBaseConstants.IdTags.CARD_ID,
                                        account.getApiIdentifier())
                                .queryParam(NordeaBaseConstants.QueryKeys.CONTINUATION_KEY, page),
                        HttpMethod.GET,
                        null),
                CreditCardTransactionResponse.class);
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

    public ConfirmPaymentResponse confirmPayment(String paymentId, PaymentType paymentType)
            throws PaymentException {
        try {
            return createRequestInSession(
                            Urls.CONFIRM_PAYMENT
                                    .parameter(IdTags.PAYMENT_TYPE, paymentType.toString())
                                    .parameter(IdTags.PAYMENT_ID, paymentId),
                            HttpMethod.PUT,
                            null)
                    .put(ConfirmPaymentResponse.class);
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

    private String getScopes() {
        List<String> scopes = configuration.getScopes();
        if (scopes.stream().allMatch(Scopes.AIS::equalsIgnoreCase)) {
            // Return only AIS scopes
            return getScopeWithoutPayment();
        } else if (scopes.stream()
                .allMatch(
                        scope ->
                                Scopes.AIS.equalsIgnoreCase(scope)
                                        || Scopes.PIS.equalsIgnoreCase(scope))) {
            // Return AIS + PIS scopes
            return getScope();
        } else {
            throw new IllegalArgumentException(
                    String.format(
                            "%s contain invalid scope(s), only support scopes AIS and PIS",
                            scopes.toString()));
        }
    }

    protected String getScope() {
        return QueryValues.SCOPE;
    }

    protected String getScopeWithoutPayment() {
        return QueryValues.SCOPE_WITHOUT_PAYMENT;
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
}
