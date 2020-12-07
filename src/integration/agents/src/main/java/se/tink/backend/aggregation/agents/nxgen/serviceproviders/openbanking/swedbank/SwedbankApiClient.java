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
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankConstants.HeaderValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankConstants.HeadersToSign;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankConstants.UrlParameters;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.authenticator.entities.consent.ConsentAllAccountsEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.authenticator.rpc.AuthenticationResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.authenticator.rpc.AuthenticationStatusResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.authenticator.rpc.AuthorizeRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.authenticator.rpc.ConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.authenticator.rpc.RefreshTokenRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.authenticator.rpc.TokenRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.configuration.SwedbankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.executor.payment.enums.SwedbankPaymentType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.executor.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.executor.payment.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.executor.payment.rpc.GetPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.executor.payment.rpc.GetPaymentStatusResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.executor.payment.rpc.PaymentAuthorisationResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.fetcher.transactionalaccount.rpc.AccountBalanceResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.fetcher.transactionalaccount.rpc.FetchAccountResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.fetcher.transactionalaccount.rpc.StatementResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.entity.AccessEntity;
import se.tink.backend.aggregation.agents.utils.crypto.hash.Hash;
import se.tink.backend.aggregation.api.Psd2Headers;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.configuration.agents.utils.CertificateUtils;
import se.tink.backend.aggregation.configuration.agents.utils.CertificateUtils.CANameEncoding;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants.PersistentStorageKeys;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.date.ThreadSafeDateFormat;
import se.tink.libraries.serialization.utils.SerializationUtils;

public final class SwedbankApiClient {

    private final TinkHttpClient client;
    private final PersistentStorage persistentStorage;
    private final QsealcSigner qsealcSigner;
    private final String signingCertificate;
    private final SwedbankConfiguration configuration;
    private final String redirectUrl;
    private final String signingKeyId;
    private final CredentialsRequest credentialsRequest;

    public SwedbankApiClient(
            TinkHttpClient client,
            PersistentStorage persistentStorage,
            AgentConfiguration<SwedbankConfiguration> agentConfiguration,
            QsealcSigner qsealcSigner,
            CredentialsRequest credentialsRequest) {
        this.client = client;
        this.persistentStorage = persistentStorage;
        this.qsealcSigner = qsealcSigner;
        this.configuration = agentConfiguration.getProviderSpecificConfiguration();
        this.redirectUrl = agentConfiguration.getRedirectUrl();
        this.credentialsRequest = credentialsRequest;

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
                .queryParam(
                        SwedbankConstants.QueryKeys.BIC, SwedbankConstants.BICProduction.SWEDEN);
    }

    private RequestBuilder createRequestInSession(URL url, boolean withConsent) {
        final RequestBuilder request = createRequest(url).addBearerToken(getTokenFromSession());
        if (credentialsRequest.isManual()) {
            request.header(
                            HeaderKeys.PSU_IP_ADDRESS,
                            Optional.ofNullable(credentialsRequest.getOriginatingUserIp())
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
                createRequest(SwedbankConstants.Urls.AUTHORIZATION)
                        .queryParam(
                                SwedbankConstants.QueryKeys.CLIENT_ID,
                                getConfiguration().getClientId())
                        .queryParam(
                                SwedbankConstants.QueryKeys.RESPONSE_TYPE,
                                SwedbankConstants.QueryValues.RESPONSE_TYPE_CODE)
                        .queryParam(SwedbankConstants.QueryKeys.REDIRECT_URI, getRedirectUrl())
                        .queryParam(SwedbankConstants.QueryKeys.STATE, state)
                        .get(HttpResponse.class);

        return new URL(response.getHeaders().getFirst(HttpHeaders.LOCATION));
    }

    public AuthenticationResponse authenticate(String ssn) {

        final AuthorizeRequest authorizeRequest =
                new AuthorizeRequest(
                        configuration.getClientId(),
                        getRedirectUrl(),
                        credentialsRequest.getProvider().getPayload());

        return createRequest(SwedbankConstants.Urls.AUTHORIZATION)
                .header(HeaderKeys.PSU_ID, ssn)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .post(AuthenticationResponse.class, authorizeRequest);
    }

    public AuthenticationStatusResponse collectAuthStatus(String ssn, String path) {
        return createRequest(new URL(Urls.BASE + path))
                .queryParam(QueryKeys.CLIENT_ID, configuration.getClientId())
                .header(HeaderKeys.PSU_ID, ssn)
                .get(AuthenticationStatusResponse.class);
    }

    public ConsentRequest createConsentRequest() {
        return new ConsentRequest<>(
                false,
                LocalDate.now()
                        .plusDays(SwedbankConstants.TimeValues.CONSENT_DURATION_IN_DAYS)
                        .toString(),
                SwedbankConstants.BodyParameter.FREQUENCY_PER_DAY,
                SwedbankConstants.BodyParameter.COMBINED_SERVICE_INDICATOR,
                new ConsentAllAccountsEntity(SwedbankConstants.BodyParameter.ALL_ACCOUNTS));
    }

    public ConsentRequest createConsentRequest(List<String> list) {
        return new ConsentRequest<>(
                true,
                LocalDate.now()
                        .plusDays(SwedbankConstants.TimeValues.CONSENT_DURATION_IN_DAYS)
                        .toString(),
                SwedbankConstants.BodyParameter.FREQUENCY_PER_DAY,
                SwedbankConstants.BodyParameter.COMBINED_SERVICE_INDICATOR,
                new AccessEntity.Builder().addIbans(list).build());
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

    public HttpResponse getTransactions(String accountId, Date fromDate, Date toDate) {
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
                .get(HttpResponse.class);
    }

    public HttpResponse getTransactions(String endPoint) {
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

    public AuthenticationResponse startAuthorization(String endpoint) {
        return createRequestInSession(new URL(Urls.BASE.concat(endpoint)), true)
                .header(HeaderKeys.TPP_REDIRECT_URI, getRedirectUrl())
                .header(HeaderKeys.TPP_NOK_REDIRECT_URI, getRedirectUrl())
                .header(HeaderKeys.TPP_REDIRECT_PREFERRED, HeaderValues.TPP_REDIRECT_PREFERRED)
                .body(new AuthorizeRequest(), MediaType.APPLICATION_JSON_TYPE)
                .put(AuthenticationResponse.class);
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

    private RequestBuilder getPaymentAuthorizationRequestBuilder(URL url, String state) {
        String tppRedirectUrl =
                new URL(getRedirectUrl())
                        .queryParam(QueryKeys.STATE, state)
                        .queryParam(QueryKeys.CODE, QueryValues.RESPONSE_TYPE_CODE)
                        .toString();

        return getPaymentRequestBuilder(url)
                .header(HeaderKeys.TPP_REDIRECT_URI, tppRedirectUrl)
                .header(HeaderKeys.TPP_NOK_REDIRECT_URI, tppRedirectUrl);
    }

    public CreatePaymentResponse createPayment(
            CreatePaymentRequest createPaymentRequest, SwedbankPaymentType swedbankPaymentType) {

        String requestId = UUID.randomUUID().toString();
        String digest = createDigest(SerializationUtils.serializeToString(createPaymentRequest));
        Date date = new Date();
        Map<String, Object> headers = getHeaders(requestId, digest, date);

        return client.request(
                        Urls.INITIATE_PAYMENT.parameter(
                                UrlParameters.PAYMENT_TYPE, swedbankPaymentType.toString()))
                .addBearerToken(getTokenFromSession())
                .queryParam(SwedbankConstants.QueryKeys.BIC, SwedbankConstants.BICProduction.SWEDEN)
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
    }

    public GetPaymentResponse getPayment(
            String paymentId, SwedbankPaymentType swedbankPaymentType) {
        return getPaymentRequestBuilder(
                        Urls.GET_PAYMENT
                                .parameter(
                                        UrlParameters.PAYMENT_TYPE, swedbankPaymentType.toString())
                                .parameter(UrlParameters.PAYMENT_ID, paymentId))
                .get(GetPaymentResponse.class);
    }

    public GetPaymentStatusResponse getPaymentStatus(
            String paymentId, SwedbankPaymentType swedbankPaymentType) {
        return getPaymentRequestBuilder(
                        Urls.GET_PAYMENT_STATUS
                                .parameter(
                                        UrlParameters.PAYMENT_TYPE, swedbankPaymentType.toString())
                                .parameter(UrlParameters.PAYMENT_ID, paymentId))
                .get(GetPaymentStatusResponse.class);
    }

    public PaymentAuthorisationResponse startPaymentAuthorisation(
            String paymentId, SwedbankPaymentType swedbankPaymentType, String state) {
        return getPaymentAuthorizationRequestBuilder(
                        Urls.INITIATE_PAYMENT_AUTH
                                .parameter(
                                        UrlParameters.PAYMENT_TYPE, swedbankPaymentType.toString())
                                .parameter(UrlParameters.PAYMENT_ID, paymentId),
                        state)
                .post(PaymentAuthorisationResponse.class);
    }

    private String createDigest(String body) {
        return String.format(
                "SHA-256=".concat("%s"), Base64.getEncoder().encodeToString(Hash.sha256(body)));
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
