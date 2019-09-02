package se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.SwedbankConstants.BICProduction;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.SwedbankConstants.BICSandbox;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.SwedbankConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.SwedbankConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.SwedbankConstants.HeaderValues;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.SwedbankConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.SwedbankConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.SwedbankConstants.UrlParameters;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.SwedbankConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.authenticator.entity.consent.ConsentAllAccountsEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.authenticator.rpc.ConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.authenticator.rpc.RefreshTokenRequest;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.authenticator.rpc.TokenRequest;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.configuration.SwedbankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.executor.payment.enums.SwedbankPaymentType;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.executor.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.executor.payment.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.executor.payment.rpc.GetPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.executor.payment.rpc.GetPaymentStatusResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.executor.payment.rpc.PaymentAuthorisationResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.fetcher.transactionalaccount.entity.account.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.fetcher.transactionalaccount.entity.transaction.Response;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.fetcher.transactionalaccount.rpc.AccountBalanceResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.fetcher.transactionalaccount.rpc.FetchAccountResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.fetcher.transactionalaccount.rpc.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.entity.AccessEntity;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.date.ThreadSafeDateFormat;

public final class SwedbankApiClient {
    private final TinkHttpClient client;
    private final PersistentStorage persistentStorage;
    private SwedbankConfiguration configuration;
    private FetchAccountResponse accounts;

    public SwedbankApiClient(TinkHttpClient client, PersistentStorage persistentStorage) {
        this.client = client;
        this.persistentStorage = persistentStorage;
    }

    public void setConfiguration(SwedbankConfiguration configuration) {
        this.configuration = configuration;
    }

    public SwedbankConfiguration getConfiguration() {
        return Optional.ofNullable(configuration)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    private OAuth2Token getTokenFromSession() {
        return persistentStorage
                .get(SwedbankConstants.StorageKeys.OAUTH_TOKEN, OAuth2Token.class)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_TOKEN));
    }

    private RequestBuilder createRequest(URL url) {
        return client.request(url)
                .header(SwedbankConstants.HeaderKeys.X_REQUEST_ID, getRequestId())
                .header(HeaderKeys.PSU_IP_ADDRESS, HeaderValues.PSU_IP_ADDRESS)
                .header(HeaderKeys.PSU_USER_AGENT, HeaderValues.PSU_USER_AGENT)
                .header(SwedbankConstants.HeaderKeys.DATE, getHeaderTimeStamp())
                .accept(MediaType.APPLICATION_JSON);
    }

    private RequestBuilder createRequestInSession(URL url) {
        return createRequest(url).addBearerToken(getTokenFromSession());
    }

    private RequestBuilder createRequestInSessionWithConsent(URL url) {
        return createRequestInSession(url)
                .header(
                        SwedbankConstants.HeaderKeys.CONSENT_ID,
                        persistentStorage.get(StorageKeys.CONSENT));
    }

    public FetchAccountResponse fetchAccounts() {
        if (accounts == null) {
            accounts =
                    createRequestInSessionWithConsent(SwedbankConstants.Urls.ACCOUNTS)
                            .header(SwedbankConstants.HeaderKeys.DATE, getHeaderTimeStamp())
                            .queryParam(
                                    SwedbankConstants.QueryKeys.BIC,
                                    SwedbankConstants.BICProduction.SWEDEN)
                            .get(FetchAccountResponse.class);
        }
        return accounts;
    }

    public boolean checkIfConsentIsApproved(String consentId) {
        return createRequestInSession(
                        Urls.CONSENT_STATUS.parameter(UrlParameters.CONSENT_ID, consentId))
                .queryParam(QueryKeys.BIC, BICProduction.SWEDEN)
                .get(ConsentResponse.class)
                .getConsentStatus()
                .equalsIgnoreCase(SwedbankConstants.ConsentStatus.VALID);
    }

    public List<String> mapAccountResponseToIbanList(FetchAccountResponse accounts) {
        return accounts.getAccountList().stream()
                .map(AccountEntity::getIban)
                .collect(Collectors.toList());
    }

    public List<String> mapAccountResponseToResourceList(FetchAccountResponse accounts) {
        return accounts.getAccountList().stream()
                .map(AccountEntity::getResourceId)
                .collect(Collectors.toList());
    }

    public URL getAuthorizeUrl(String state) {
        return new URL(
                client.request(
                                createRequest(SwedbankConstants.Urls.AUTHORIZE)
                                        .header(
                                                SwedbankConstants.HeaderKeys.DATE,
                                                getHeaderTimeStamp())
                                        .queryParam(
                                                SwedbankConstants.QueryKeys.BIC,
                                                SwedbankConstants.BICProduction.SWEDEN)
                                        .queryParam(SwedbankConstants.QueryKeys.STATE, state)
                                        .queryParam(
                                                SwedbankConstants.QueryKeys.CLIENT_ID,
                                                getConfiguration().getClientId())
                                        .queryParam(
                                                SwedbankConstants.QueryKeys.REDIRECT_URI,
                                                getConfiguration().getRedirectUrl())
                                        .queryParam(
                                                SwedbankConstants.QueryKeys.RESPONSE_TYPE,
                                                SwedbankConstants.QueryValues.RESPONSE_TYPE_CODE)
                                        .queryParam(
                                                SwedbankConstants.QueryKeys.SCOPE,
                                                SwedbankConstants.QueryValues.SCOPE_PSD2)
                                        .getUrl())
                        .get(HttpResponse.class)
                        .getHeaders()
                        .getFirst(SwedbankConstants.HeaderResponse.LOCATION));
    }

    public ConsentRequest createConsentRequest() {
        return new ConsentRequest<>(
                SwedbankConstants.BodyParameter.RECURRING_INDICATOR,
                LocalDateTime.now()
                        .plusDays(SwedbankConstants.TimeValues.CONSENT_DURATION_IN_DAYS)
                        .toString(),
                SwedbankConstants.BodyParameter.FREQUENCY_PER_DAY,
                SwedbankConstants.BodyParameter.COMBINED_SERVICE_INDICATOR,
                new ConsentAllAccountsEntity(SwedbankConstants.BodyParameter.ALL_ACCOUNTS));
    }

    public ConsentRequest createConsentRequest(List<String> list) {
        return new ConsentRequest<>(
                SwedbankConstants.BodyParameter.RECURRING_INDICATOR,
                LocalDateTime.now()
                        .plusDays(SwedbankConstants.TimeValues.CONSENT_DURATION_IN_DAYS)
                        .toString(),
                SwedbankConstants.BodyParameter.FREQUENCY_PER_DAY,
                SwedbankConstants.BodyParameter.COMBINED_SERVICE_INDICATOR,
                new AccessEntity.Builder().addIbans(list).build());
    }

    public ConsentResponse getConsent(List<String> list) {
        return createRequestInSession(SwedbankConstants.Urls.CONSENTS)
                .type(MediaType.APPLICATION_JSON)
                .header(HeaderKeys.TPP_REDIRECT_URI, configuration.getRedirectUrl())
                .header(HeaderKeys.TPP_NOK_REDIRECT_URI, configuration.getRedirectUrl())
                .queryParam(SwedbankConstants.QueryKeys.BIC, SwedbankConstants.BICProduction.SWEDEN)
                .post(ConsentResponse.class, createConsentRequest(list));
    }

    public ConsentResponse createFirstConsent() {
        return createRequestInSession(SwedbankConstants.Urls.CONSENTS)
                .type(MediaType.APPLICATION_JSON)
                .queryParam(SwedbankConstants.QueryKeys.BIC, SwedbankConstants.BICProduction.SWEDEN)
                .post(ConsentResponse.class, createConsentRequest());
    }

    public OAuth2Token exchangeCodeForToken(String code) {

        TokenRequest request =
                new TokenRequest(
                        getConfiguration().getClientId(),
                        getConfiguration().getClientSecret(),
                        getConfiguration().getRedirectUrl(),
                        code);

        return createRequest(SwedbankConstants.Urls.TOKEN)
                .queryParam(SwedbankConstants.QueryKeys.BIC, SwedbankConstants.BICProduction.SWEDEN)
                .type(MediaType.APPLICATION_FORM_URLENCODED)
                .post(TokenResponse.class, request.toData())
                .toTinkToken();
    }

    public AccountBalanceResponse getAccountBalance(String accountId) {
        return createRequestInSessionWithConsent(
                        Urls.ACCOUNT_BALANCES.parameter(UrlParameters.ACCOUNT_ID, accountId))
                .queryParam(SwedbankConstants.QueryKeys.BIC, SwedbankConstants.BICProduction.SWEDEN)
                .get(AccountBalanceResponse.class);
    }

    public FetchTransactionsResponse getTransactions(String accountId, Date fromDate, Date toDate) {
        return createRequestInSessionWithConsent(
                        Urls.ACCOUNT_TRANSACTIONS.parameter(UrlParameters.ACCOUNT_ID, accountId))
                .queryParam(SwedbankConstants.QueryKeys.BIC, SwedbankConstants.BICProduction.SWEDEN)
                .queryParam(
                        SwedbankConstants.HeaderKeys.FROM_DATE,
                        ThreadSafeDateFormat.FORMATTER_DAILY.format(fromDate))
                .queryParam(
                        SwedbankConstants.HeaderKeys.TO_DATE,
                        ThreadSafeDateFormat.FORMATTER_DAILY.format(toDate))
                .queryParam(
                        SwedbankConstants.QueryKeys.BOOKING_STATUS,
                        SwedbankConstants.QueryValues.BOOKING_STATUS_BOTH)
                .get(FetchTransactionsResponse.class);
    }

    public Response startScaTransactionRequest(String accountId, Date fromDate, Date toDate) {

        return createRequestInSessionWithConsent(
                        Urls.ACCOUNT_TRANSACTIONS.parameter(UrlParameters.ACCOUNT_ID, accountId))
                .queryParam(SwedbankConstants.QueryKeys.BIC, SwedbankConstants.BICProduction.SWEDEN)
                .queryParam(
                        SwedbankConstants.HeaderKeys.FROM_DATE,
                        ThreadSafeDateFormat.FORMATTER_DAILY.format(fromDate))
                .queryParam(
                        SwedbankConstants.HeaderKeys.TO_DATE,
                        ThreadSafeDateFormat.FORMATTER_DAILY.format(toDate))
                .queryParam(
                        SwedbankConstants.QueryKeys.BOOKING_STATUS,
                        SwedbankConstants.QueryValues.BOOKING_STATUS_BOTH)
                .post(Response.class);
    }

    public ConsentResponse startAuthorization(String endpoint) {
        return createRequestInSessionWithConsent(new URL(Urls.BASE.concat(endpoint)))
                .header(HeaderKeys.TPP_REDIRECT_URI, configuration.getRedirectUrl())
                .header(HeaderKeys.TPP_NOK_REDIRECT_URI, configuration.getRedirectUrl())
                .queryParam(SwedbankConstants.QueryKeys.BIC, SwedbankConstants.BICProduction.SWEDEN)
                .post(ConsentResponse.class);
    }

    public boolean checkStatus(String statusEndpoint) {
        return createRequestInSessionWithConsent(new URL(Urls.BASE.concat(statusEndpoint)))
                .queryParam(SwedbankConstants.QueryKeys.BIC, SwedbankConstants.BICProduction.SWEDEN)
                .get(ConsentResponse.class)
                .getStatementStatus()
                .equalsIgnoreCase(SwedbankConstants.ConsentStatus.SIGNED);
    }

    public OAuth2Token refreshToken(String refreshToken) {

        RefreshTokenRequest request =
                new RefreshTokenRequest(
                        refreshToken,
                        getConfiguration().getClientId(),
                        getConfiguration().getClientSecret(),
                        getConfiguration().getRedirectUrl());

        return createRequest(SwedbankConstants.Urls.TOKEN)
                .queryParam(SwedbankConstants.QueryKeys.BIC, SwedbankConstants.BICProduction.SWEDEN)
                .type(MediaType.APPLICATION_FORM_URLENCODED)
                .post(TokenResponse.class, request.toData())
                .toTinkToken();
    }

    private RequestBuilder getPaymentRequestBuilder(URL url) {
        return createRequest(url)
                .addBearerToken(getTokenFromSession())
                .queryParam(QueryKeys.BIC, BICSandbox.SWEDEN)
                .header(SwedbankConstants.HeaderKeys.DATE, getHeaderTimeStamp())
                .header(HeaderKeys.X_REQUEST_ID, getRequestId())
                .header(HeaderKeys.PSU_IP_ADDRESS, configuration.getPsuIpAddress())
                .header(HeaderKeys.TPP_REDIRECT_URI, configuration.getRedirectUrl())
                .header(HeaderKeys.TPP_NOK_REDIRECT_URI, configuration.getRedirectUrl())
                .header(HeaderKeys.PSU_USER_AGENT, HeaderValues.PSU_USER_AGENT);
    }

    public CreatePaymentResponse createPayment(
            CreatePaymentRequest createPaymentRequest, SwedbankPaymentType swedbankPaymentType) {
        return getPaymentRequestBuilder(
                        Urls.INITIATE_PAYMENT.parameter(
                                UrlParameters.PAYMENT_TYPE, swedbankPaymentType.toString()))
                .post(CreatePaymentResponse.class, createPaymentRequest);
    }

    public GetPaymentResponse getPayment(String paymentId) {
        return getPaymentRequestBuilder(
                        Urls.GET_PAYMENT.parameter(UrlParameters.PAYMENT_ID, paymentId))
                .get(GetPaymentResponse.class);
    }

    public GetPaymentStatusResponse getPaymentStatus(String paymentId) {
        return getPaymentRequestBuilder(
                        Urls.GET_PAYMENT_STATUS.parameter(UrlParameters.PAYMENT_ID, paymentId))
                .get(GetPaymentStatusResponse.class);
    }

    public PaymentAuthorisationResponse startPaymentAuthorisation(String paymentId) {
        return getPaymentRequestBuilder(
                        Urls.INITIATE_PAYMENT_AUTH.parameter(UrlParameters.PAYMENT_ID, paymentId))
                .post(PaymentAuthorisationResponse.class);
    }

    private String getRequestId() {
        return java.util.UUID.randomUUID().toString();
    }

    private String getHeaderTimeStamp() {
        return new SimpleDateFormat(SwedbankConstants.Format.HEADER_TIMESTAMP).format(new Date());
    }
}
