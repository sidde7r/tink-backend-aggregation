package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseConstants.QueryValues.SCOPE;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseConstants.QueryValues.SCOPE_WITHOUT_PAYMENT;

import com.google.common.base.Strings;
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
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseConstants.ApiService;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseConstants.HeaderValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseConstants.Signature;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.authenticator.rpc.GetTokenForm;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.authenticator.rpc.GetTokenResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.authenticator.rpc.RefreshTokenForm;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.configuration.NordeaBaseConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.executor.payment.rpc.ConfirmPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.executor.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.executor.payment.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.executor.payment.rpc.GetPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.executor.payment.rpc.GetPaymentsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.fetcher.transactionalaccount.rpc.GetAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.fetcher.transactionalaccount.rpc.GetTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.filters.BankSideFailureFilter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.rpc.NordeaErrorResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.util.SignatureUtil;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.configuration.eidas.proxy.EidasProxyConfiguration;
import se.tink.backend.aggregation.eidassigner.identity.EidasIdentity;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.filter.filters.ServiceUnavailableBankServiceErrorFilter;
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
    private EidasProxyConfiguration eidasProxyConfiguration;
    private EidasIdentity eidasIdentity;

    public NordeaBaseApiClient(TinkHttpClient client, PersistentStorage persistentStorage) {
        this.client = client;
        this.persistentStorage = persistentStorage;

        this.client.addFilter(new BankSideFailureFilter());
        this.client.addFilter(new ServiceUnavailableBankServiceErrorFilter());
    }

    public NordeaBaseConfiguration getConfiguration() {
        return Optional.ofNullable(configuration)
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        NordeaBaseConstants.ErrorMessages.MISSING_CONFIGURATION));
    }

    public String getRedirectUrl() {
        return Optional.ofNullable(redirectUrl)
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        NordeaBaseConstants.ErrorMessages.MISSING_CONFIGURATION));
    }

    public void setConfiguration(
            AgentConfiguration<NordeaBaseConfiguration> agentConfiguration,
            EidasProxyConfiguration eidasProxyConfiguration,
            EidasIdentity eidasIdentity) {
        this.configuration = agentConfiguration.getProviderSpecificConfiguration();
        this.redirectUrl = agentConfiguration.getRedirectUrl();
        this.eidasProxyConfiguration = eidasProxyConfiguration;
        this.eidasIdentity = eidasIdentity;
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
                        NordeaBaseConstants.HeaderKeys.AUTHORIZATION,
                        token.getTokenType() + " " + token.getAccessToken());
    }

    private RequestBuilder createTokenRequest(String body) {
        return createRequest(NordeaBaseConstants.Urls.GET_TOKEN, HttpMethod.POST, body);
    }

    public URL getAuthorizeUrl(String state, String country) {
        return client.request(
                        NordeaBaseConstants.Urls.AUTHORIZE
                                .queryParam(
                                        NordeaBaseConstants.QueryKeys.CLIENT_ID,
                                        configuration.getClientId())
                                .queryParam(NordeaBaseConstants.QueryKeys.STATE, state)
                                .queryParam(
                                        NordeaBaseConstants.QueryKeys.DURATION,
                                        NordeaBaseConstants.QueryValues.DURATION_MINUTES)
                                .queryParam(NordeaBaseConstants.QueryKeys.COUNTRY, country)
                                .queryParam(NordeaBaseConstants.QueryKeys.SCOPE, getScopes())
                                .queryParam(
                                        QueryKeys.MAX_TX_HISTORY,
                                        QueryValues.FETCH_NUMBER_OF_MONTHS)
                                .queryParam(
                                        NordeaBaseConstants.QueryKeys.REDIRECT_URI,
                                        getRedirectUrl()))
                .getUrl();
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

    public GetAccountsResponse getAccounts() {
        return requestRefreshableGet(
                createRequestInSession(NordeaBaseConstants.Urls.GET_ACCOUNTS, HttpMethod.GET, null),
                GetAccountsResponse.class);
    }

    public GetAccountsResponse getCorporateAccounts() {
        return createRequestInSession(Urls.GET_CORPORATE_ACCOUNTS, HttpMethod.GET, null)
                .get(GetAccountsResponse.class);
    }

    public GetTransactionsResponse getCorporateTransactions(
            TransactionalAccount account, String key) {
        URL url =
                Optional.ofNullable(key)
                        .map(k -> new URL(Urls.BASE_CORPORATE_URL + k))
                        .orElse(
                                NordeaBaseConstants.Urls.GET_CORPORATE_TRANSACTIONS.parameter(
                                        NordeaBaseConstants.IdTags.ACCOUNT_ID,
                                        account.getApiIdentifier()));

        return createRequestInSession(url, HttpMethod.GET, null).get(GetTransactionsResponse.class);
    }

    public GetTransactionsResponse getTransactions(TransactionalAccount account, String key) {
        URL url =
                Optional.ofNullable(key)
                        .map(k -> new URL(NordeaBaseConstants.Urls.BASE_URL + k))
                        .orElse(
                                NordeaBaseConstants.Urls.GET_TRANSACTIONS.parameter(
                                        NordeaBaseConstants.IdTags.ACCOUNT_ID,
                                        account.getApiIdentifier()));

        RequestBuilder request = createRequestInSession(url, HttpMethod.GET, null);

        return requestRefreshableGet(request, GetTransactionsResponse.class);
    }

    @Override
    public void storeToken(OAuth2Token token) {
        persistentStorage.rotateStorageValue(
                OAuth2Constants.PersistentStorageKeys.OAUTH_2_TOKEN, token);
    }

    @Override
    public OAuth2Token getStoredToken() {
        return persistentStorage
                .get(OAuth2Constants.PersistentStorageKeys.OAUTH_2_TOKEN, OAuth2Token.class)
                .orElseThrow(() -> new IllegalStateException("Cannot find token!"));
    }

    public CreatePaymentResponse createPayment(
            CreatePaymentRequest createPaymentRequest, PaymentType paymentType)
            throws PaymentException {
        String body = SerializationUtils.serializeToString(createPaymentRequest);
        try {
            return createRequestInSession(
                            NordeaBaseConstants.Urls.INITIATE_PAYMENT.parameter(
                                    NordeaBaseConstants.IdTags.PAYMENT_TYPE,
                                    paymentType.toString()),
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
                            NordeaBaseConstants.Urls.CONFIRM_PAYMENT
                                    .parameter(
                                            NordeaBaseConstants.IdTags.PAYMENT_TYPE,
                                            paymentType.toString())
                                    .parameter(NordeaBaseConstants.IdTags.PAYMENT_ID, paymentId),
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
                            NordeaBaseConstants.Urls.GET_PAYMENT
                                    .parameter(
                                            NordeaBaseConstants.IdTags.PAYMENT_TYPE,
                                            paymentType.toString())
                                    .parameter(NordeaBaseConstants.IdTags.PAYMENT_ID, paymentId),
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
                            NordeaBaseConstants.Urls.GET_PAYMENTS.parameter(
                                    NordeaBaseConstants.IdTags.PAYMENT_TYPE,
                                    paymentType.toString()),
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
                    && body.toLowerCase()
                            .contains(NordeaBaseConstants.ErrorMessages.TOKEN_EXPIRED.toLowerCase())
                    && body.toLowerCase()
                            .contains(NordeaBaseConstants.ErrorCodes.TOKEN_EXPIRED.toLowerCase())) {
                return;
            }
        }
        throw hre;
    }

    private String getScopes() {
        List<String> scopes = configuration.getScopes();
        if (scopes.stream()
                .allMatch(scope -> NordeaBaseConstants.Scopes.AIS.equalsIgnoreCase(scope))) {
            // Return only AIS scopes
            return SCOPE_WITHOUT_PAYMENT;
        } else if (scopes.stream()
                .allMatch(
                        scope ->
                                NordeaBaseConstants.Scopes.AIS.equalsIgnoreCase(scope)
                                        || NordeaBaseConstants.Scopes.PIS.equalsIgnoreCase(
                                                scope))) {
            // Return AIS + PIS scopes
            return SCOPE;
        } else {
            throw new IllegalArgumentException(
                    String.format(
                            "%s contain invalid scope(s), only support scopes AIS and PIS",
                            scopes.toString()));
        }
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
                    eidasProxyConfiguration,
                    eidasIdentity);
        } else {
            return SignatureUtil.createGetSignature(
                    getConfiguration().getClientId(),
                    httpMethod,
                    requestUrl.toUri(),
                    date,
                    eidasProxyConfiguration,
                    eidasIdentity);
        }
    }

    private String getServerDate() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat(Signature.DATE_FORMAT, Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone(Signature.TIMEZONE));
        return dateFormat.format(calendar.getTime());
    }
}
