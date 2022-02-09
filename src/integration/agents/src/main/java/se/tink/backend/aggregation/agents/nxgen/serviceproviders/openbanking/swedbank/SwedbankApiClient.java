package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank;

import com.google.common.base.Strings;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.ws.rs.core.MediaType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHeaders;
import se.tink.backend.aggregation.agents.consent.generators.se.swedbank.SwedbankConsentGenerator;
import se.tink.backend.aggregation.agents.consent.suppliers.ItemsSupplier;
import se.tink.backend.aggregation.agents.exceptions.payment.CreditorValidationException;
import se.tink.backend.aggregation.agents.exceptions.payment.InsufficientFundsException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentRejectedException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankConstants.BICProduction;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankConstants.BodyParameter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankConstants.ErrorCodes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankConstants.HeaderValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankConstants.HeadersToSign;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankConstants.UrlParameters;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.authenticator.entities.SwedbankAccessAccountCheckEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.authenticator.entities.SwedbankAccessEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.authenticator.entities.SwedbankTransactionsOver90DayAccessEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.authenticator.entities.consent.ConsentAllAccountsEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.authenticator.rpc.AuthenticationResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.authenticator.rpc.AuthenticationStatusResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.authenticator.rpc.AuthorizeRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.authenticator.rpc.ConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.authenticator.rpc.ConsentRequestForTransactionsOver90Days;
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
import se.tink.backend.aggregation.utils.qrcode.QrCodeParser;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.cryptography.hash.Hash;
import se.tink.libraries.date.ThreadSafeDateFormat;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.libraries.signableoperation.enums.InternalStatus;

@Slf4j
public class SwedbankApiClient implements SwedbankOpenBankingPaymentApiClient {

    private final TinkHttpClient client;
    private final PersistentStorage persistentStorage;
    private final QsealcSigner qsealcSigner;
    private final String signingCertificate;
    protected final SwedbankConfiguration configuration;
    private final String redirectUrl;
    private final String signingKeyId;
    protected final AgentComponentProvider componentProvider;
    private final String bic;
    protected final String authenticationMethodId;
    protected final String bookingStatus;
    private final SwedbankMarketConfiguration marketConfiguration;

    public SwedbankApiClient(
            TinkHttpClient client,
            PersistentStorage persistentStorage,
            AgentConfiguration<SwedbankConfiguration> agentConfiguration,
            QsealcSigner qsealcSigner,
            AgentComponentProvider componentProvider,
            SwedbankMarketConfiguration marketConfiguration) {
        this.client = client;
        this.persistentStorage = persistentStorage;
        this.qsealcSigner = qsealcSigner;
        this.configuration = agentConfiguration.getProviderSpecificConfiguration();
        this.redirectUrl = agentConfiguration.getRedirectUrl();
        this.componentProvider = componentProvider;
        this.bic = marketConfiguration.getBIC();
        this.authenticationMethodId = marketConfiguration.getAuthenticationMethodId();
        this.bookingStatus = marketConfiguration.getBookingStatus();
        this.marketConfiguration = marketConfiguration;
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

    protected String getRedirectUrl() {
        return Optional.ofNullable(redirectUrl)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    private OAuth2Token getTokenFromSession() {
        return persistentStorage
                .get(PersistentStorageKeys.OAUTH_2_TOKEN, OAuth2Token.class)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_TOKEN));
    }

    protected RequestBuilder createRequest(URL url) {
        return client.request(url)
                .header(SwedbankConstants.HeaderKeys.X_REQUEST_ID, Psd2Headers.getRequestId())
                .header(HttpHeaders.DATE, getFormattedDate(new Date()))
                .accept(MediaType.APPLICATION_JSON)
                .queryParam(SwedbankConstants.QueryKeys.BIC, bic);
    }

    protected RequestBuilder createRequestInSession(URL url, boolean withConsent) {
        final RequestBuilder request = createRequest(url).addBearerToken(getTokenFromSession());

        if (isUserPresent()) {
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

    public String fetchQRCodeImage(String authorizeId) {
        RequestBuilder builder =
                client.request(
                                Urls.AUTHORIZATION_QR_IMAGE.parameter(
                                        UrlParameters.AUTHORIZE_ID, authorizeId))
                        .header(HeaderKeys.X_REQUEST_ID, Psd2Headers.getRequestId())
                        .header(HeaderKeys.DATE, getFormattedDate(new Date()))
                        .queryParam(QueryKeys.CLIENT_ID, getConfiguration().getClientId())
                        .queryParam(QueryKeys.BIC, BICProduction.SWEDEN);
        HttpResponse response = builder.get(HttpResponse.class);
        try {
            byte[] bytes = IOUtils.toByteArray(response.getBodyInputStream());
            return QrCodeParser.decodeQRCode(Base64.getEncoder().encodeToString(bytes));
        } catch (IOException e) {
            log.warn("Could not download QR code. ", e);
            throw new IllegalStateException(e);
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

    public String getConsentStatus(String consentId) {
        return createRequestInSession(
                        Urls.CONSENT_STATUS.parameter(UrlParameters.CONSENT_ID, consentId), false)
                .get(ConsentResponse.class)
                .getConsentStatus();
    }

    public String getScaStatus(String statusLink) {
        return createRequestInSession(new URL(Urls.BASE.concat(statusLink)), true)
                .get(AuthenticationResponse.class)
                .getScaStatus();
    }

    public AuthenticationResponse authenticateDecoupled(String ssn) {
        // If the provider is swedbank-ob, then default bankId to 08999 to prevent savingsbank
        // customer to login with single engagement
        String bankId = isSwedbank() ? SwedbankConstants.BANK_IDS.get(0) : "";
        AuthorizeRequest.AuthorizeRequestBuilder requestBuilder =
                AuthorizeRequest.builder()
                        .clientID(configuration.getClientId())
                        .redirectUri(getRedirectUrl())
                        .authenticationMethodId(authenticationMethodId)
                        .bankId(bankId)
                        .scope(getScopes(componentProvider));

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

    private RequestBuilder createDecoupledAuthorizeRequest(String ssn, String authorizeId) {
        return createRequest(
                        Urls.AUTHORIZATION_DECOUPLED_AUTHORIZE_ID.parameter(
                                UrlParameters.AUTHORIZE_ID, authorizeId))
                .queryParam(QueryKeys.CLIENT_ID, configuration.getClientId())
                .header(HeaderKeys.PSU_ID, ssn);
    }

    public AuthenticationStatusResponse collectBalticAuthStatus(String ssn, String path) {
        return createAuthBaseRequest(ssn, path).get(AuthenticationStatusResponse.class);
    }

    public AuthenticationStatusResponse collectAuthStatus(String ssn, String authorizeId) {
        return createDecoupledAuthorizeRequest(ssn, authorizeId)
                .get(AuthenticationStatusResponse.class);
    }

    public AuthenticationStatusResponse supplyBankId(
            String ssn, String authorizeId, String bankId) {
        return createDecoupledAuthorizeRequest(ssn, authorizeId)
                .body(new SupplyBankIdRequest(bankId), MediaType.APPLICATION_JSON)
                .put(AuthenticationStatusResponse.class);
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
                BodyParameter.FREQUENCY_PER_DAY_ALL_ACCOUNTS,
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
                getAccessEntity(list, false));
    }

    // for consent over 90 days, recurringIndicator must be false. Values validUntil and
    // frequencyPerDay are not compatible with this consent type.
    public ConsentRequestForTransactionsOver90Days createConsentRequestForTransactionsOver90Days(
            List<String> list) {
        return new ConsentRequestForTransactionsOver90Days<>(
                false,
                SwedbankConstants.BodyParameter.COMBINED_SERVICE_INDICATOR,
                getAccessEntity(list, true));
    }

    public Object getAccessEntity(List<String> list, boolean isTransactionOver90Days) {
        return ItemsSupplier.get(componentProvider.getCredentialsRequest())
                        .contains(RefreshableItem.CHECKING_TRANSACTIONS)
                ? getTransactionsAccessEntity(list, isTransactionOver90Days)
                : new SwedbankAccessAccountCheckEntity().addIbans(list);
    }

    public Object getTransactionsAccessEntity(List<String> list, boolean isTransactionOver90Days) {
        return isTransactionOver90Days
                ? new SwedbankTransactionsOver90DayAccessEntity().addIbans(list)
                : new SwedbankAccessEntity().addIbans(list);
    }

    /**
     * Get consent for fetching accounts, balances, and transactions. Has to be approved by SCA for
     * every account. In order to not count as a call without PSU interaction the headers
     * PSU-IP-Address, PSU-IP-Port, PSU-User-Agent, and PSU-Http-Method are required.
     */
    public ConsentResponse getConsentAccountDetails(List<String> list) {
        return getConsent().post(ConsentResponse.class, createConsentRequest(list));
    }

    /* the agent should make sure to not call this method when CHECKING_TRANSACTIONS scope is not
    provided. This part should be handled by the consent management library. Due to a possible bug,
    the agent calls for transactions even when only CHECKING_ACCOUNTS scope is granted. This should
    be fixed in TC-5664 which will also fix calling of this method. */

    /**
     * Get consent for transactions over 90 days. Has to be approved by SCA for every account. In
     * order to not count as a call without PSU interaction the headers PSU-IP-Address, PSU-IP-Port,
     * PSU-User-Agent, and PSU-Http-Method are required.
     */
    public ConsentResponse getConsentTransactionOver90Days(List<String> list) {
        return getConsent()
                .post(ConsentResponse.class, createConsentRequestForTransactionsOver90Days(list));
    }

    /**
     * Get consent to fetch a list of all user's available accounts. Does not require SCA as no
     * transaction or balance info can be retrieved with this consent. In order to not count as a
     * call without PSU interaction the headers PSU-IP-Address, PSU-IP-Port, PSU-User-Agent, and
     * PSU-Http-Method are required.
     */
    public ConsentResponse getConsentAllAccounts() {
        return getConsent().post(ConsentResponse.class, createConsentRequest());
    }

    private RequestBuilder getConsent() {
        return createRequestInSession(SwedbankConstants.Urls.CONSENTS, false)
                .queryParam(QueryKeys.APP_ID, getConfiguration().getClientId())
                .type(MediaType.APPLICATION_JSON)
                .header(HeaderKeys.TPP_REDIRECT_URI, getRedirectUrl())
                .header(HeaderKeys.TPP_REDIRECT_PREFERRED, HeaderValues.TPP_REDIRECT_PREFERRED)
                .header(HeaderKeys.TPP_NOK_REDIRECT_URI, getRedirectUrl());
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
        if (isUserPresent()) {
            RequestBuilder requestBuilder =
                    createRequestInSession(
                                    Urls.ACCOUNT_TRANSACTIONS.parameter(
                                            UrlParameters.ACCOUNT_ID, accountId),
                                    false)
                            .queryParam(SwedbankConstants.HeaderKeys.FROM_DATE, fromDate.toString())
                            .queryParam(SwedbankConstants.HeaderKeys.TO_DATE, toDate.toString())
                            .queryParam(SwedbankConstants.QueryKeys.BOOKING_STATUS, bookingStatus)
                            .header(
                                    HeaderKeys.CONSENT_ID,
                                    persistentStorage.get(
                                            StorageKeys.CONSENT_TRANSACTIONS_OVER_90_DAYS));
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

    private boolean isUserPresent() {
        return componentProvider.getUser().isPresent();
    }

    public HttpResponse getOfflineTransactions(String endPoint) {
        return createRequestInSession(new URL(Urls.BASE.concat(endPoint)), false)
                .header(
                        HeaderKeys.CONSENT_ID,
                        persistentStorage.get(StorageKeys.CONSENT_TRANSACTIONS_OVER_90_DAYS))
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
        Set<String> set = new HashSet<>();
        set.add(SwedbankConstants.SWEDBANK_OB_PROVIDER_NAME);
        set.add(SwedbankConstants.SWEDBANK_OB_BUSINESS_PROVIDER_NAME);
        return set.contains(
                componentProvider.getCredentialsRequest().getCredentials().getProviderName());
    }

    @Override
    public AuthenticationResponse startPaymentAuthorization(String endpoint)
            throws PaymentException {
        final AuthorizeRequest authorizeRequest =
                AuthorizeRequest.builder()
                        .clientID(configuration.getClientId())
                        .redirectUri(getRedirectUrl())
                        .bankId(
                                componentProvider
                                        .getCredentialsRequest()
                                        .getProvider()
                                        .getPayload())
                        .authenticationMethodId(authenticationMethodId)
                        .scope(getScopes(componentProvider))
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

    private String getScopes(AgentComponentProvider componentProvider) {
        return new SwedbankConsentGenerator(
                        componentProvider.getCredentialsRequest(), marketConfiguration.getScopes())
                .generate();
    }
}
