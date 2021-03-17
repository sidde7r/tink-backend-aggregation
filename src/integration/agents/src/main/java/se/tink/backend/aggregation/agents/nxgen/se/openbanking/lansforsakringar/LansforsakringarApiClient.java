package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.ws.rs.core.MediaType;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.LansforsakringarConstants.BodyValues;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.LansforsakringarConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.LansforsakringarConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.LansforsakringarConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.LansforsakringarConstants.HeaderValues;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.LansforsakringarConstants.IdTags;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.LansforsakringarConstants.PaymentTypes;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.LansforsakringarConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.LansforsakringarConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.LansforsakringarConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.authenticator.rpc.AuthenticateResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.authenticator.rpc.ConsentStatusResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.authenticator.rpc.RefreshTokenForm;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.authenticator.rpc.SignBasketResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.authenticator.rpc.TokenForm;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.configuration.LansforsakringarConfiguration;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.executor.payment.entities.AccountNumbersResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.executor.payment.rpc.CreateBasketResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.executor.payment.rpc.CrossBorderPaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.executor.payment.rpc.CrossBorderPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.executor.payment.rpc.DomesticGirosPaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.executor.payment.rpc.DomesticPaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.executor.payment.rpc.DomesticPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.executor.payment.rpc.GetCrossBorderPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.executor.payment.rpc.GetDomesticPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.executor.payment.rpc.GetPaymentStatusResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.fetcher.entities.BasketEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.fetcher.entities.PaymentIdEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.fetcher.rpc.GetAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.fetcher.rpc.GetBalancesResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.fetcher.rpc.GetTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.rpc.ErrorResponse;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class LansforsakringarApiClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(LansforsakringarApiClient.class);

    private final TinkHttpClient client;
    private final Credentials credentials;
    private final LansforsakringarStorageHelper storageHelper;
    private final LansforsakringarUserIpInformation userIpInformation;

    private LansforsakringarConfiguration configuration;
    private String redirectUrl;

    LansforsakringarApiClient(
            TinkHttpClient client,
            Credentials credentials,
            LansforsakringarStorageHelper storageHelper,
            LansforsakringarUserIpInformation userIpInformation) {
        this.client = client;
        this.credentials = credentials;
        this.storageHelper = storageHelper;
        this.userIpInformation = userIpInformation;
    }

    public Credentials getCredentials() {
        return Optional.ofNullable(credentials)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CREDENTIALS));
    }

    public LansforsakringarConfiguration getConfiguration() {
        return Optional.ofNullable(configuration)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    private String getRedirectUrl() {
        return Optional.ofNullable(redirectUrl)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    public void setConfiguration(
            AgentConfiguration<LansforsakringarConfiguration> agentConfiguration) {
        this.configuration = agentConfiguration.getProviderSpecificConfiguration();
        this.redirectUrl = agentConfiguration.getRedirectUrl();
    }

    public RequestBuilder createRequest(URL url) {
        RequestBuilder builder =
                client.request(url)
                        .accept(MediaType.APPLICATION_JSON)
                        .type(MediaType.APPLICATION_JSON)
                        .header(HeaderKeys.PSU_USER_AGENT, HeaderValues.PSU_USER_AGENT)
                        .header(HeaderKeys.TPP_NOK_REDIRECT_URI, getRedirectUrl())
                        .header(HeaderKeys.X_REQUEST_ID, UUID.randomUUID());
        setPsuIp(builder);

        return builder;
    }

    private void setPsuIp(RequestBuilder builder) {
        // LF API does not want to receive PSU_IP_ADDRESS for BG Refreshes
        if (userIpInformation.isManualRequest()) {
            builder.header(HeaderKeys.PSU_IP_ADDRESS, userIpInformation.getUserIp());
        }
    }

    private RequestBuilder createRequestInSession(URL url) {
        return createRequest(url).addBearerToken(getTokenFromStorage());
    }

    private RequestBuilder createRequestInSession(String url) {
        return createRequestInSession(new URL(url));
    }

    public ConsentResponse getConsent() {
        return createRequest(new URL(LansforsakringarConstants.Urls.CONSENT))
                .header(
                        LansforsakringarConstants.HeaderKeys.PSU_ID,
                        getCredentials().getField(Field.Key.USERNAME))
                .header(
                        LansforsakringarConstants.HeaderKeys.PSU_ID_TYPE,
                        LansforsakringarConstants.HeaderValues.PSU_ID_TYPE)
                .header(LansforsakringarConstants.HeaderKeys.TPP_REDIRECT_URI, getRedirectUrl())
                .header(LansforsakringarConstants.HeaderKeys.TPP_EXPLICIT_AUTH_PREFERRED, false)
                .post(ConsentResponse.class, BodyValues.EMPTY_BODY);
    }

    public ConsentStatusResponse getConsentStatus() {
        String consentId = storageHelper.getConsentId();
        if (StringUtils.isEmpty(consentId)) {
            return null;
        }

        return client.request(new URL(Urls.CONSENT_STATUS).parameter(IdTags.CONSENT_ID, consentId))
                .accept(MediaType.APPLICATION_JSON)
                .addBearerToken(getTokenFromStorage())
                .header(HeaderKeys.PSU_USER_AGENT, HeaderValues.PSU_USER_AGENT)
                .header(HeaderKeys.X_REQUEST_ID, UUID.randomUUID())
                .get(ConsentStatusResponse.class);
    }

    public URL buildAuthorizeUrl(String state, String authorizationId) {
        return createRequest(new URL(Urls.AUTHORIZATION))
                .queryParam(
                        LansforsakringarConstants.QueryKeys.CLIENT_ID,
                        getConfiguration().getClientId())
                .queryParam(
                        LansforsakringarConstants.QueryKeys.RESPONSE_TYPE,
                        LansforsakringarConstants.QueryValues.RESPONSE_TYPE)
                .queryParam(LansforsakringarConstants.QueryKeys.AUTHORIZATION_ID, authorizationId)
                .queryParam(LansforsakringarConstants.QueryKeys.REDIRECT_URI, getRedirectUrl())
                .queryParam(LansforsakringarConstants.QueryKeys.STATE, state)
                .type(MediaType.APPLICATION_FORM_URLENCODED)
                .getUrl();
    }

    public OAuth2Token exchangeAuthorizationCode(String code) {
        final TokenForm form =
                TokenForm.builder()
                        .setClientId(getConfiguration().getClientId())
                        .setGrantType(FormValues.AUTHORIZATION_CODE)
                        .setCode(code)
                        .setClientSecret(getConfiguration().getClientSecret())
                        .setRedirectUri(getRedirectUrl())
                        .build();

        URL tokenUrl = new URL(Urls.TOKEN);
        return postToken(form, tokenUrl);
    }

    public OAuth2Token refreshToken(final String refreshToken) {
        final RefreshTokenForm form =
                RefreshTokenForm.builder()
                        .setClientId(getConfiguration().getClientId())
                        .setGrantType(FormValues.REFRESH_TOKEN)
                        .setClientSecret(getConfiguration().getClientSecret())
                        .setRefreshToken(refreshToken)
                        .build();

        URL tokenUrl = new URL(Urls.TOKEN);
        return postToken(form, tokenUrl);
    }

    public OAuth2Token postToken(Object form, URL tokenUrl) {
        try {
            return client.request(tokenUrl)
                    .header(HeaderKeys.X_REQUEST_ID, UUID.randomUUID())
                    .header(HeaderKeys.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                    .header(HeaderKeys.CACHE_CONTROL, HeaderValues.NO_CACHE)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(form)
                    .post(AuthenticateResponse.class)
                    .toTinkToken();
        } catch (HttpResponseException e) {
            tryToHandleTokenFetchFailureDueToBankIssuesAndExpiredAuthCode(e);
            throw e;
        } catch (HttpClientException e) {
            LOGGER.warn("Unhandled Client exception", e);
            throw BankServiceError.BANK_SIDE_FAILURE.exception();
        }
    }

    /*
    LF has some issues with replying after request is sent. We are trying to retry that multiple times and when
    finally they replies - we are receiving message that authorization is expired.

    Because our implementation is correct, there is almost no chance that EXPIRED_AUTHORIZATION_CODE message will be returned
    in the first attempt. It covers only retry situation.
     */
    private void tryToHandleTokenFetchFailureDueToBankIssuesAndExpiredAuthCode(
            HttpResponseException e) {
        if (e.getResponse().getStatus() == HttpStatus.SC_BAD_REQUEST) {
            ErrorResponse errorResponse = e.getResponse().getBody(ErrorResponse.class);
            if (ErrorMessages.EXPIRED_AUTHORIZATION_CODE.equals(
                    errorResponse.getErrorDescription())) {
                throw BankServiceError.BANK_SIDE_FAILURE.exception();
            }
        }
    }

    public GetAccountsResponse getAccounts() {
        Optional<GetAccountsResponse> maybeAccounts = storageHelper.getStoredAccounts();
        if (maybeAccounts.isPresent()) {
            return maybeAccounts.get();
        }

        GetAccountsResponse getAccountsResponse =
                createRequestInSession(Urls.GET_ACCOUNTS)
                        .header(HeaderKeys.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED)
                        .header(HeaderKeys.CONSENT_ID, storageHelper.getConsentId())
                        .header(HeaderKeys.CACHE_CONTROL, HeaderValues.NO_CACHE)
                        .get(GetAccountsResponse.class);

        storageHelper.storeAccounts(getAccountsResponse);
        return getAccountsResponse;
    }

    public GetBalancesResponse getBalances(String resourceId) {
        Optional<GetBalancesResponse> maybeBalanceResponse =
                storageHelper.getStoredBalanceResponse(resourceId);
        if (maybeBalanceResponse.isPresent()) {
            storageHelper.removeBalanceResponseFromStorage(resourceId);
            return maybeBalanceResponse.get();
        }

        URL balancesUrl = new URL(Urls.GET_BALANCES).parameter(IdTags.ACCOUNT_ID, resourceId);

        return createRequestInSession(balancesUrl)
                .header(HeaderKeys.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED)
                .header(HeaderKeys.CONSENT_ID, storageHelper.getConsentId())
                .header(HeaderKeys.CACHE_CONTROL, HeaderValues.NO_CACHE)
                .get(GetBalancesResponse.class);
    }

    public TransactionKeyPaginatorResponse<String> getTransactionsForKey(String key) {
        return createRequestInSession(Urls.BASE_API_URL + key)
                .header(HeaderKeys.CONSENT_ID, storageHelper.getConsentId())
                .queryParam(QueryKeys.BOOKING_STATUS, QueryValues.BOTH)
                .get(GetTransactionsResponse.class);
    }

    public TransactionKeyPaginatorResponse<String> getTransactionsForAccount(
            String apiIdentifier, LocalDateTimeSource localDateTimeSource) {
        final URL url = new URL(Urls.GET_TRANSACTIONS).parameter(IdTags.ACCOUNT_ID, apiIdentifier);

        return createRequestInSession(url)
                .header(HeaderKeys.CONSENT_ID, storageHelper.getConsentId())
                .queryParam(QueryKeys.DATE_FROM, getDateFromForTransactions(localDateTimeSource))
                .queryParam(QueryKeys.BOOKING_STATUS, QueryValues.BOTH)
                .get(GetTransactionsResponse.class);
    }

    private String getDateFromForTransactions(LocalDateTimeSource localDateTimeSource) {
        LocalDateTime now = localDateTimeSource.now();
        if (userIpInformation.isManualRequest()) {
            now = now.minusMonths(LansforsakringarConstants.MONTHS_TO_FETCH);
        } else {
            now = now.minusDays(LansforsakringarConstants.DAYS_TO_FETCH_BG);
        }
        return now.toLocalDate().toString();
    }

    private OAuth2Token getTokenFromStorage() {
        return storageHelper
                .getOAuth2Token()
                .orElseThrow(
                        () -> {
                            LOGGER.warn(ErrorMessages.MISSING_TOKEN);
                            return new IllegalStateException(
                                    SessionError.SESSION_EXPIRED.exception());
                        });
    }

    public DomesticPaymentResponse createDomesticPayment(
            DomesticPaymentRequest domesticPaymentRequest) {
        return createRequestInSession(
                        new URL(Urls.CREATE_PAYMENT)
                                .parameter(
                                        IdTags.PAYMENT_TYPE,
                                        PaymentTypes.DOMESTIC_CREDIT_TRANSFERS))
                .header(HeaderKeys.TPP_REDIRECT_URI, getRedirectUrl())
                .post(DomesticPaymentResponse.class, domesticPaymentRequest);
    }

    public DomesticPaymentResponse createDomesticGirosPayment(
            DomesticGirosPaymentRequest domesticGirosPaymentRequest) {
        return createRequestInSession(
                        new URL(Urls.CREATE_PAYMENT)
                                .parameter(IdTags.PAYMENT_TYPE, PaymentTypes.DOMESTIC_GIROS))
                .header(HeaderKeys.TPP_REDIRECT_URI, getRedirectUrl())
                .post(DomesticPaymentResponse.class, domesticGirosPaymentRequest);
    }

    public CrossBorderPaymentResponse createCrossBorderPayment(
            CrossBorderPaymentRequest crossBorderPaymentRequest) {
        return createRequestInSession(
                        new URL(Urls.CREATE_PAYMENT)
                                .parameter(
                                        IdTags.PAYMENT_TYPE,
                                        PaymentTypes.CROSS_BORDER_CREDIT_TRANSFERS))
                .header(HeaderKeys.TPP_REDIRECT_URI, getRedirectUrl())
                .post(CrossBorderPaymentResponse.class, crossBorderPaymentRequest);
    }

    public GetPaymentStatusResponse getPaymentStatus(String paymentId) {
        return createRequestInSession(
                        new URL(Urls.GET_PAYMENT_STATUS).parameter(IdTags.PAYMENT_ID, paymentId))
                .get(GetPaymentStatusResponse.class);
    }

    public GetCrossBorderPaymentResponse getCrossBorderPayment(String paymentId) {
        return createRequestInSession(
                        new URL(Urls.GET_PAYMENT).parameter(IdTags.PAYMENT_ID, paymentId))
                .get(GetCrossBorderPaymentResponse.class);
    }

    public GetDomesticPaymentResponse getDomesticPayment(String paymentId) {
        return createRequestInSession(
                        new URL(Urls.GET_PAYMENT).parameter(IdTags.PAYMENT_ID, paymentId))
                .get(GetDomesticPaymentResponse.class);
    }

    public CreateBasketResponse createSigningBasket(String paymentId) {

        PaymentIdEntity paymentIdEntity = new PaymentIdEntity();
        paymentIdEntity.setPaymentId(paymentId);

        List<PaymentIdEntity> list = new ArrayList<>();
        list.add(paymentIdEntity);

        BasketEntity basketEntity = new BasketEntity();
        basketEntity.setTransactions(list);

        return createRequestInSession(new URL(Urls.CREATE_SIGNING_BASKET))
                .header(HeaderKeys.TPP_REDIRECT_URI, getRedirectUrl())
                .post(CreateBasketResponse.class, basketEntity);
    }

    public SignBasketResponse signBasket(String authorizationUrl, String basketId) {

        return createRequestInSession(new URL(Urls.BASE_API_URL + authorizationUrl))
                .header(HeaderKeys.TPP_REDIRECT_URI, getRedirectUrl())
                .header(HeaderKeys.BASKET_ID, basketId)
                .post(SignBasketResponse.class, BodyValues.EMPTY_BODY);
    }

    public AccountNumbersResponse getAccountNumbers() {
        return createRequestInSession(new URL(Urls.GET_ACCOUNT_NUMBERS))
                .header("Consent-ID", storageHelper.getConsentId())
                .get(AccountNumbersResponse.class);
    }
}
