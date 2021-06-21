package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank;

import com.google.common.base.Strings;
import java.security.cert.CertificateException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.ws.rs.core.MediaType;
import org.apache.http.HttpHeaders;
import se.tink.backend.aggregation.agents.exceptions.payment.CreditorValidationException;
import se.tink.backend.aggregation.agents.exceptions.payment.InsufficientFundsException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentRejectedException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankConstants.ErrorCodes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankConstants.HeaderValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankConstants.HeadersToSign;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankConstants.RequestValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankConstants.UrlParameters;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.authenticator.entities.SwedbankAccessEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.authenticator.entities.consent.ConsentAllAccountsEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.authenticator.rpc.AuthenticationResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.authenticator.rpc.AuthenticationStatusResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.authenticator.rpc.AuthorizeRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.authenticator.rpc.ConsentAuthorizeRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.authenticator.rpc.ConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.authenticator.rpc.RefreshTokenRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.authenticator.rpc.SupplyBankIdRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.authenticator.rpc.TokenRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.common.SwedbankOpenBankingPaymentApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.configuration.SwedbankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.executor.payment.enums.SwedbankPaymentType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.executor.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.executor.payment.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.executor.payment.rpc.GetPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.executor.payment.rpc.PaymentAuthorisationResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.executor.payment.rpc.PaymentStatusResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.fetcher.transactionalaccount.rpc.AccountBalanceResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.fetcher.transactionalaccount.rpc.FetchAccountResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.fetcher.transactionalaccount.rpc.FetchOnlineTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.fetcher.transactionalaccount.rpc.StatementResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.rpc.GenericResponse;
import se.tink.backend.aggregation.agents.utils.crypto.hash.Hash;
import se.tink.backend.aggregation.api.Psd2Headers;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.configuration.agents.utils.CertificateUtils;
import se.tink.backend.aggregation.configuration.agents.utils.CertificateUtils.CANameEncoding;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants.PersistentStorageKeys;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.date.ThreadSafeDateFormat;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.libraries.signableoperation.enums.InternalStatus;

public final class SwedbankApiClient implements SwedbankOpenBankingPaymentApiClient {

    private final TinkHttpClient client;
    private final PersistentStorage persistentStorage;
    private final QsealcSigner qsealcSigner;
    private final String signingCertificate;
    private final SwedbankConfiguration configuration;
    private final String redirectUrl;
    private final String signingKeyId;
    private final AgentComponentProvider componentProvider;
    private final CredentialsRequest credentialsRequest;
    private final String bic;
    private final String authenticationMethodId;

    public SwedbankApiClient(
            TinkHttpClient client,
            PersistentStorage persistentStorage,
            AgentConfiguration<SwedbankConfiguration> agentConfiguration,
            QsealcSigner qsealcSigner,
            AgentComponentProvider componentProvider,
            CredentialsRequest credentialsRequest,
            String bic,
            String authenticationMethodId) {
        this.client = client;
        this.persistentStorage = persistentStorage;
        this.qsealcSigner = qsealcSigner;
        this.configuration = agentConfiguration.getProviderSpecificConfiguration();
        this.redirectUrl = agentConfiguration.getRedirectUrl();
        this.componentProvider = componentProvider;
        this.credentialsRequest = credentialsRequest;
        this.bic = bic;
        this.authenticationMethodId = authenticationMethodId;

        try {
            this.signingCertificate =
                    CertificateUtils.getDerEncodedCertFromBase64EncodedCertificate(
                            agentConfiguration.getQsealc());
            this.signingKeyId =
                    Psd2Headers.getTppCertificateKeyId(
                            agentConfiguration.getQsealc(), 16, CANameEncoding.BASE64_IF_NOT_ASCII);
        } catch (CertificateException e) {
            throw new IllegalStateException(ErrorMessages.INVALID_CONFIGURATION, e);
        }

        client.setFollowRedirects(false);
    }

    public SwedbankConfiguration getConfiguration() {
        return Optional.ofNullable(configuration)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    private String getRedirectUrl() {
        return Optional.ofNullable(redirectUrl)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    private OAuth2Token getTokenFromSession() {
        return persistentStorage
                .get(PersistentStorageKeys.OAUTH_2_TOKEN, OAuth2Token.class)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_TOKEN));
    }

    private RequestBuilder createRequest(URL url) {
        return client.request(url)
                .header(SwedbankConstants.HeaderKeys.X_REQUEST_ID, Psd2Headers.getRequestId())
                .header(HttpHeaders.DATE, getFormattedDate(new Date()))
                .accept(MediaType.APPLICATION_JSON)
                .queryParam(SwedbankConstants.QueryKeys.BIC, bic);
    }

    private RequestBuilder createRequestInSession(URL url, boolean withConsent) {
        final RequestBuilder request = createRequest(url).addBearerToken(getTokenFromSession());

        if (componentProvider.getCredentialsRequest().getUserAvailability().isUserPresent()) {
            request.header(
                            HeaderKeys.PSU_IP_ADDRESS,
                            Optional.ofNullable(
                                            componentProvider
                                                    .getCredentialsRequest()
                                                    .getOriginatingUserIp())
                                    .orElse(HeaderValues.PSU_IP_ADDRESS))
                    .header(HeaderKeys.PSU_USER_AGENT, HeaderValues.PSU_USER_AGENT)
                    .header(HeaderKeys.PSU_IP_PORT, HeaderValues.PSU_IP_PORT)
                    .header(HeaderKeys.PSU_HTTP_METHOD, HeaderValues.PSU_HTTP_METHOD);
        }
        if (withConsent) {
            return request.header(
                    SwedbankConstants.HeaderKeys.CONSENT_ID,
                    persistentStorage.get(StorageKeys.CONSENT));
        } else {
            return request;
        }
    }

    public FetchAccountResponse fetchAccounts() {
        return createRequestInSession(SwedbankConstants.Urls.ACCOUNTS, true)
                .queryParam(QueryKeys.APP_ID, getConfiguration().getClientId())
                .get(FetchAccountResponse.class);
    }

    public boolean isConsentValid() {
        String consentId = persistentStorage.get(StorageKeys.CONSENT);
        if (Strings.isNullOrEmpty(consentId)) {
            return false;
        }
        return createRequestInSession(
                        Urls.CONSENT_STATUS.parameter(UrlParameters.CONSENT_ID, consentId), false)
                .get(ConsentResponse.class)
                .getConsentStatus()
                .equalsIgnoreCase(SwedbankConstants.ConsentStatus.VALID);
    }

    public String getScaStatus(String statusLink) {
        return createRequestInSession(new URL(Urls.BASE.concat(statusLink)), true)
                .get(AuthenticationResponse.class)
                .getScaStatus();
    }

    public URL getAuthorizeUrl(String state) {
        HttpResponse response =
                createRequest(SwedbankConstants.Urls.AUTHORIZATION_REDIRECT)
                        .queryParam(
                                SwedbankConstants.QueryKeys.CLIENT_ID,
                                getConfiguration().getClientId())
                        .queryParam(
                                SwedbankConstants.QueryKeys.RESPONSE_TYPE,
                                SwedbankConstants.QueryValues.RESPONSE_TYPE_CODE)
                        .queryParam(SwedbankConstants.QueryKeys.SCOPE, RequestValues.ALL_SCOPES)
                        .queryParam(SwedbankConstants.QueryKeys.REDIRECT_URI, getRedirectUrl())
                        .queryParam(SwedbankConstants.QueryKeys.STATE, state)
                        .get(HttpResponse.class);

        return new URL(response.getHeaders().getFirst(HttpHeaders.LOCATION));
    }

    public AuthenticationResponse authenticateDecoupled(
            String ssn, String market, String personalId) {
        // If the provider is swedbank-ob, then default bankId to 08999 to prevent savingsbank
        // customer to login with single engagement
        String bankId = isSwedbank() ? SwedbankConstants.BANK_IDS.get(0) : "";
        AuthorizeRequest.AuthorizeRequestBuilder requestBuilder =
                AuthorizeRequest.builder()
                        .clientID(configuration.getClientId())
                        .redirectUri(getRedirectUrl())
                        .authenticationMethodId(authenticationMethodId);

        // TODO: fixme
        if (market.equalsIgnoreCase("SE")) {
            requestBuilder = requestBuilder.bankId(bankId).scope(RequestValues.ALL_SCOPES);
        } else if (market.equalsIgnoreCase("EE")) {
            requestBuilder =
                    requestBuilder.personalID(personalId).scope(RequestValues.ALL_ACCOUNTS_SCOPES);
        }

        return createRequest(SwedbankConstants.Urls.AUTHORIZATION_DECOUPLED)
                .header(HeaderKeys.PSU_ID, ssn)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .post(AuthenticationResponse.class, requestBuilder.build());
    }

    private RequestBuilder createAuthBaseRequest(String ssn, String path) {
        return createRequest(new URL(Urls.BASE + path))
                .queryParam(QueryKeys.CLIENT_ID, configuration.getClientId())
                .header(HeaderKeys.PSU_ID, ssn);
    }

    public AuthenticationStatusResponse collectAuthStatus(String ssn, String path) {
        return createAuthBaseRequest(ssn, path).get(AuthenticationStatusResponse.class);
    }

    public AuthenticationStatusResponse supplyBankId(String ssn, String path, String bankId) {
        return createAuthBaseRequest(ssn, path)
                .body(new SupplyBankIdRequest(bankId), MediaType.APPLICATION_JSON)
                .put(AuthenticationStatusResponse.class);
    }

    public AuthenticationResponse authorizeConsent(String url) {
        return createRequestInSession(new URL(Urls.BASE.concat(url)), true)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .put(
                        AuthenticationResponse.class,
                        new ConsentAuthorizeRequest(authenticationMethodId));
    }

    public ConsentRequest createConsentRequest() {
        return new ConsentRequest<>(
                false,
                componentProvider
                        .getLocalDateTimeSource()
                        .now()
                        .toLocalDate()
                        .plusDays(SwedbankConstants.TimeValues.CONSENT_DURATION_IN_DAYS)
                        .toString(),
                SwedbankConstants.BodyParameter.FREQUENCY_PER_DAY,
                SwedbankConstants.BodyParameter.COMBINED_SERVICE_INDICATOR,
                new ConsentAllAccountsEntity(SwedbankConstants.BodyParameter.ALL_ACCOUNTS));
    }

    public ConsentRequest createConsentRequest(List<String> list) {
        return new ConsentRequest<>(
                true,
                componentProvider
                        .getLocalDateTimeSource()
                        .now()
                        .toLocalDate()
                        .plusDays(SwedbankConstants.TimeValues.CONSENT_DURATION_IN_DAYS)
                        .toString(),
                SwedbankConstants.BodyParameter.FREQUENCY_PER_DAY,
                SwedbankConstants.BodyParameter.COMBINED_SERVICE_INDICATOR,
                new SwedbankAccessEntity().addIbans(list));
    }

    /**
     * Get consent for fetching accounts, balances, and transactions. Has to be approved by SCA for
     * every account. In order to not count as a call without PSU interaction the headers
     * PSU-IP-Address, PSU-IP-Port, PSU-User-Agent, and PSU-Http-Method are required.
     */
    public ConsentResponse getConsentAccountDetails(List<String> list) {
        return createRequestInSession(SwedbankConstants.Urls.CONSENTS, false)
                .queryParam(QueryKeys.APP_ID, getConfiguration().getClientId())
                .type(MediaType.APPLICATION_JSON)
                .header(HeaderKeys.TPP_REDIRECT_URI, getRedirectUrl())
                .header(HeaderKeys.TPP_REDIRECT_PREFERRED, HeaderValues.TPP_REDIRECT_PREFERRED)
                .header(HeaderKeys.TPP_NOK_REDIRECT_URI, getRedirectUrl())
                .post(ConsentResponse.class, createConsentRequest(list));
    }

    /**
     * Get consent to fetch a list of all user's available accounts. Does not require SCA as no
     * transaction or balance info can be retrieved with this consent. In order to not count as a
     * call without PSU interaction the headers PSU-IP-Address, PSU-IP-Port, PSU-User-Agent, and
     * PSU-Http-Method are required.
     */
    public ConsentResponse getConsentAllAccounts() {
        return createRequestInSession(SwedbankConstants.Urls.CONSENTS, false)
                .queryParam(QueryKeys.APP_ID, getConfiguration().getClientId())
                .header(HeaderKeys.TPP_REDIRECT_URI, getRedirectUrl())
                .header(HeaderKeys.TPP_REDIRECT_PREFERRED, HeaderValues.TPP_REDIRECT_PREFERRED)
                .type(MediaType.APPLICATION_JSON)
                .post(ConsentResponse.class, createConsentRequest());
    }

    public OAuth2Token exchangeCodeForToken(String code) {

        TokenRequest request =
                new TokenRequest(
                        getConfiguration().getClientId(),
                        getConfiguration().getClientSecret(),
                        getRedirectUrl(),
                        code);

        return createRequest(SwedbankConstants.Urls.TOKEN)
                .type(MediaType.APPLICATION_FORM_URLENCODED)
                .queryParams(request.toData())
                .post(TokenResponse.class)
                .toTinkToken();
    }

    public AccountBalanceResponse getAccountBalance(String accountId) {
        return createRequestInSession(
                        Urls.ACCOUNT_BALANCES.parameter(UrlParameters.ACCOUNT_ID, accountId), true)
                .get(AccountBalanceResponse.class);
    }

    public FetchOnlineTransactionsResponse getOnlineTransactions(
            String accountId, LocalDate fromDate, LocalDate toDate) {
        return createRequestInSession(
                        Urls.ACCOUNT_TRANSACTIONS.parameter(UrlParameters.ACCOUNT_ID, accountId),
                        true)
                .queryParam(SwedbankConstants.HeaderKeys.FROM_DATE, fromDate.toString())
                .queryParam(SwedbankConstants.HeaderKeys.TO_DATE, toDate.toString())
                .queryParam(
                        SwedbankConstants.QueryKeys.BOOKING_STATUS,
                        SwedbankConstants.QueryValues.BOOKING_STATUS_BOTH)
                .get(FetchOnlineTransactionsResponse.class);
    }

    public Optional<StatementResponse> postOrGetOfflineStatement(
            String accountId, LocalDate fromDate, LocalDate toDate) {

        // Swedbank doesn't allow offline statement without PSU involvement
        if (componentProvider.getCredentialsRequest().getUserAvailability().isUserPresent()) {
            RequestBuilder requestBuilder =
                    createRequestInSession(
                                    Urls.ACCOUNT_TRANSACTIONS.parameter(
                                            UrlParameters.ACCOUNT_ID, accountId),
                                    true)
                            .queryParam(SwedbankConstants.HeaderKeys.FROM_DATE, fromDate.toString())
                            .queryParam(SwedbankConstants.HeaderKeys.TO_DATE, toDate.toString())
                            .queryParam(
                                    SwedbankConstants.QueryKeys.BOOKING_STATUS,
                                    SwedbankConstants.QueryValues.BOOKING_STATUS_BOTH);

            try {
                return Optional.of(requestBuilder.post(StatementResponse.class));
            } catch (HttpResponseException hre) {
                GenericResponse errorResponse = hre.getResponse().getBody(GenericResponse.class);
                if (errorResponse.isResourceAlreadySigned()) {
                    return Optional.of(requestBuilder.get(StatementResponse.class));
                }
                throw new IllegalStateException(hre);
            }
        }
        return Optional.empty();
    }

    public HttpResponse getOfflineTransactions(String endPoint) {
        return createRequestInSession(new URL(Urls.BASE.concat(endPoint)), true)
                .get(HttpResponse.class);
    }

    public StatementResponse startScaTransactionRequest(
            String accountId, Date fromDate, Date toDate) {

        return createRequestInSession(
                        Urls.ACCOUNT_TRANSACTIONS.parameter(UrlParameters.ACCOUNT_ID, accountId),
                        true)
                .queryParam(
                        SwedbankConstants.HeaderKeys.FROM_DATE,
                        ThreadSafeDateFormat.FORMATTER_DAILY.format(fromDate))
                .queryParam(
                        SwedbankConstants.HeaderKeys.TO_DATE,
                        ThreadSafeDateFormat.FORMATTER_DAILY.format(toDate))
                .queryParam(
                        SwedbankConstants.QueryKeys.BOOKING_STATUS,
                        SwedbankConstants.QueryValues.BOOKING_STATUS_BOTH)
                .header(HeaderKeys.TPP_REDIRECT_URI, getRedirectUrl())
                .header(HeaderKeys.TPP_NOK_REDIRECT_URI, getRedirectUrl())
                .post(StatementResponse.class);
    }

    public boolean isSwedbank() {
        return SwedbankConstants.SWEDBANK_OB_PROVIDER_NAME.equals(
                componentProvider.getCredentialsRequest().getCredentials().getProviderName());
    }

    @Override
    public AuthenticationResponse startPaymentAuthorization(String endpoint)
            throws PaymentException {
        final AuthorizeRequest authorizeRequest =
                AuthorizeRequest.builder()
                        .clientID(configuration.getClientId())
                        .redirectUri(getRedirectUrl())
                        .bankId(credentialsRequest.getProvider().getPayload())
                        .build();
        try {

            return createRequestInSession(new URL(Urls.BASE.concat(endpoint)), true)
                    .header(HeaderKeys.TPP_REDIRECT_URI, getRedirectUrl())
                    .header(HeaderKeys.TPP_NOK_REDIRECT_URI, getRedirectUrl())
                    .header(HeaderKeys.TPP_REDIRECT_PREFERRED, HeaderValues.TPP_REDIRECT_PREFERRED)
                    .body(AuthorizeRequest.builder().build(), MediaType.APPLICATION_JSON_TYPE)
                    .put(AuthenticationResponse.class, authorizeRequest);
        } catch (HttpResponseException e) {
            handleBankSideErrorCodes(e);
            throw e;
        }
    }

    public AuthenticationResponse getScaResponse(String statusLink) {
        return createRequestInSession(new URL(Urls.BASE.concat(statusLink)), true)
                .get(AuthenticationResponse.class);
    }

    public OAuth2Token refreshToken(String refreshToken) {

        RefreshTokenRequest request =
                new RefreshTokenRequest(
                        refreshToken,
                        getConfiguration().getClientId(),
                        getConfiguration().getClientSecret(),
                        getRedirectUrl());

        return createRequest(SwedbankConstants.Urls.TOKEN)
                .type(MediaType.APPLICATION_FORM_URLENCODED)
                .post(TokenResponse.class, request.toData())
                .toTinkToken();
    }

    private RequestBuilder getPaymentRequestBuilder(URL url) {
        return createRequestInSession(url, false)
                .header(HeaderKeys.TPP_REDIRECT_URI, getRedirectUrl())
                .header(HeaderKeys.TPP_NOK_REDIRECT_URI, getRedirectUrl());
    }

    private RequestBuilder getPaymentAuthorizationRequestBuilder(
            URL url, String state, boolean isRedirect) {
        String tppRedirectUrl =
                new URL(getRedirectUrl())
                        .queryParam(QueryKeys.STATE, state)
                        .queryParam(QueryKeys.CODE, QueryValues.RESPONSE_TYPE_CODE)
                        .toString();

        return getPaymentRequestBuilder(url)
                .header(HeaderKeys.TPP_REDIRECT_URI, tppRedirectUrl)
                .header(HeaderKeys.TPP_NOK_REDIRECT_URI, tppRedirectUrl)
                .header(HeaderKeys.TPP_REDIRECT_PREFERRED, isRedirect);
    }

    @Override
    public CreatePaymentResponse createPayment(
            CreatePaymentRequest createPaymentRequest, SwedbankPaymentType swedbankPaymentType)
            throws PaymentException {

        String requestId = UUID.randomUUID().toString();
        String digest = createDigest(SerializationUtils.serializeToString(createPaymentRequest));
        Date date = new Date();
        Map<String, Object> headers = getHeaders(requestId, digest, date);
        try {
            return client.request(
                            Urls.INITIATE_PAYMENT.parameter(
                                    UrlParameters.PAYMENT_TYPE, swedbankPaymentType.toString()))
                    .addBearerToken(getTokenFromSession())
                    .queryParam(SwedbankConstants.QueryKeys.BIC, bic)
                    .header(HeaderKeys.PSU_IP_ADDRESS, HeaderValues.PSU_IP_ADDRESS)
                    .header(HeaderKeys.PSU_USER_AGENT, HeaderValues.PSU_USER_AGENT)
                    .header(HeaderKeys.X_REQUEST_ID, requestId)
                    .header(HeaderKeys.DATE, getFormattedDate(date))
                    .header(HeaderKeys.TPP_REDIRECT_URI, getRedirectUrl())
                    .header(HeaderKeys.DIGEST, digest)
                    .header(HeaderKeys.SIGNATURE, generateSignatureHeader(headers))
                    .type(MediaType.APPLICATION_JSON_TYPE)
                    .accept(MediaType.APPLICATION_JSON)
                    .post(
                            CreatePaymentResponse.class,
                            SerializationUtils.serializeToString(createPaymentRequest));
        } catch (HttpResponseException e) {
            handleBankSideErrorCodes(e);
            throw e;
        }
    }

    @Override
    public GetPaymentResponse getPayment(String paymentId, SwedbankPaymentType swedbankPaymentType)
            throws PaymentException {
        try {
            return getPaymentRequestBuilder(
                            Urls.GET_PAYMENT
                                    .parameter(
                                            UrlParameters.PAYMENT_TYPE,
                                            swedbankPaymentType.toString())
                                    .parameter(UrlParameters.PAYMENT_ID, paymentId))
                    .get(GetPaymentResponse.class);
        } catch (HttpResponseException e) {
            handleBankSideErrorCodes(e);
            throw e;
        }
    }

    @Override
    public PaymentStatusResponse getPaymentStatus(
            String paymentId, SwedbankPaymentType swedbankPaymentType) throws PaymentException {
        try {
            return getPaymentRequestBuilder(
                            Urls.GET_PAYMENT_STATUS
                                    .parameter(
                                            UrlParameters.PAYMENT_TYPE,
                                            swedbankPaymentType.toString())
                                    .parameter(UrlParameters.PAYMENT_ID, paymentId))
                    .get(PaymentStatusResponse.class);
        } catch (HttpResponseException e) {
            handleBankSideErrorCodes(e);
            throw e;
        }
    }

    @Override
    public PaymentStatusResponse deletePayment(String paymentProduct, String paymentId) {
        try {
            return getPaymentRequestBuilder(
                            Urls.DELETE_PAYMENT
                                    .parameter(UrlParameters.PAYMENT_TYPE, paymentProduct)
                                    .parameter(UrlParameters.PAYMENT_ID, paymentId))
                    .delete(PaymentStatusResponse.class);
        } catch (HttpResponseException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public PaymentAuthorisationResponse initiatePaymentAuthorisation(
            String paymentId,
            SwedbankPaymentType swedbankPaymentType,
            String state,
            boolean isRedirect)
            throws PaymentException {
        try {
            return getPaymentAuthorizationRequestBuilder(
                            Urls.INITIATE_PAYMENT_AUTH
                                    .parameter(
                                            UrlParameters.PAYMENT_TYPE,
                                            swedbankPaymentType.toString())
                                    .parameter(UrlParameters.PAYMENT_ID, paymentId),
                            state,
                            isRedirect)
                    .post(PaymentAuthorisationResponse.class);
        } catch (HttpResponseException e) {
            handleBankSideErrorCodes(e);
            throw e;
        }
    }

    private String createDigest(String body) {
        return String.format(
                "SHA-256=".concat("%s"), Base64.getEncoder().encodeToString(Hash.sha256(body)));
    }

    private void handleBankSideErrorCodes(HttpResponseException e) throws PaymentException {
        GenericResponse errorResponse = e.getResponse().getBody(GenericResponse.class);
        if (errorResponse.isBadRequest()) {
            throw new PaymentRejectedException(
                    errorResponse.getErrorMessage(ErrorCodes.FORMAT_ERROR),
                    InternalStatus.BANK_ERROR_CODE_NOT_HANDLED_YET);
        }
        if (errorResponse.hasInsufficientFunds()) {
            throw new InsufficientFundsException(
                    errorResponse.getErrorMessage(ErrorCodes.INSUFFICIENT_FUNDS),
                    InternalStatus.INSUFFICIENT_FUNDS);
        }
        if (errorResponse.isInvalidRecipient()) {
            throw new CreditorValidationException(
                    errorResponse.getErrorMessage(ErrorCodes.INVALID_RECIPIENT),
                    InternalStatus.INVALID_DESTINATION_ACCOUNT);
        }
        if (errorResponse.isAgreementMissing()) {
            throw new PaymentRejectedException(
                    errorResponse.getErrorMessage(ErrorCodes.MISSING_CT_AGREEMENT),
                    InternalStatus.BANK_ERROR_CODE_NOT_HANDLED_YET);
        }
        if (errorResponse.hasInvalidAuthToken()) {
            throw new PaymentRejectedException(
                    errorResponse.getErrorMessage(ErrorCodes.INVALID_AUTH_TOKEN),
                    InternalStatus.INVALID_SECURITY_TOKEN);
        }
        throw new PaymentException(
                errorResponse.getErrorText(), InternalStatus.BANK_ERROR_CODE_NOT_HANDLED_YET);
    }

    private String generateSignatureHeader(Map<String, Object> headers) {
        String signedHeaders =
                Arrays.stream(HeadersToSign.values())
                        .map(HeadersToSign::getHeader)
                        .filter(headers::containsKey)
                        .collect(Collectors.joining(" "));

        String signedHeadersWithValues =
                Arrays.stream(HeadersToSign.values())
                        .map(HeadersToSign::getHeader)
                        .filter(headers::containsKey)
                        .map(header -> String.format("%s: %s", header, headers.get(header)))
                        .collect(Collectors.joining("\n"));

        String signature = qsealcSigner.getSignatureBase64(signedHeadersWithValues.getBytes());

        return String.format(HeaderValues.SIGNATURE_HEADER, signingKeyId, signedHeaders, signature);
    }

    private Map<String, Object> getHeaders(String requestId, String digest, Date date) {
        String tppRedirectUrl = new URL(getRedirectUrl()).toString();

        Map<String, Object> headers = new HashMap<>();

        headers.put(HeaderKeys.ACCEPT, MediaType.APPLICATION_JSON);
        headers.put(HeaderKeys.CONSENT_ID, persistentStorage.get(StorageKeys.CONSENT));
        headers.put(HeaderKeys.X_REQUEST_ID, requestId);
        headers.put(HeaderKeys.DATE, getFormattedDate(date));
        headers.put(HeaderKeys.TPP_REDIRECT_URI, tppRedirectUrl);
        headers.put(HeaderKeys.DIGEST, digest);
        headers.put(HeaderKeys.TPP_SIGNATURE_CERTIFICATE, signingCertificate);

        return headers;
    }

    private String getFormattedDate(Date date) {
        String pattern = HeaderValues.DATE_PATTERN;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        return simpleDateFormat.format(date);
    }
}
