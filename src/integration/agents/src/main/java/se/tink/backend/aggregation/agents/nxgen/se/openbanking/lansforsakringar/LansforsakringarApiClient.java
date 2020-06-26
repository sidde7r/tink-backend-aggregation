package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar;

import java.util.Collection;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import javax.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.LansforsakringarConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.LansforsakringarConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.LansforsakringarConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.LansforsakringarConstants.HeaderValues;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.LansforsakringarConstants.IdTags;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.LansforsakringarConstants.PaymentTypes;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.LansforsakringarConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.LansforsakringarConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.LansforsakringarConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.LansforsakringarConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.authenticator.rpc.AuthenticateResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.authenticator.rpc.RefreshTokenForm;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.authenticator.rpc.TokenForm;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.configuration.LansforsakringarConfiguration;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.executor.payment.rpc.AuthorizePaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.executor.payment.rpc.CrossBorderPaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.executor.payment.rpc.CrossBorderPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.executor.payment.rpc.DomesticPaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.executor.payment.rpc.DomesticPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.executor.payment.rpc.GetCrossBorderPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.executor.payment.rpc.GetDomesticPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.executor.payment.rpc.GetPaymentStatusResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.fetcher.transactionalaccount.rpc.GetAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.fetcher.transactionalaccount.rpc.GetTransactionsResponse;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.date.ThreadSafeDateFormat;

public final class LansforsakringarApiClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(LansforsakringarApiClient.class);

    private final TinkHttpClient client;
    private final SessionStorage sessionStorage;
    private LansforsakringarConfiguration configuration;
    private String redirectUrl;
    private final Credentials credentials;
    private final PersistentStorage persistentStorage;

    LansforsakringarApiClient(
            TinkHttpClient client,
            SessionStorage sessionStorage,
            Credentials credentials,
            PersistentStorage persistentStorage) {
        this.client = client;
        this.sessionStorage = sessionStorage;
        this.credentials = credentials;
        this.persistentStorage = persistentStorage;
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
        return client.request(url)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .header(HeaderKeys.PSU_IP_ADDRESS, HeaderValues.PSU_IP_ADDRESS)
                .header(HeaderKeys.PSU_USER_AGENT, HeaderValues.PSU_USER_AGENT)
                .header(HeaderKeys.X_REQUEST_ID, UUID.randomUUID());
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
                .post(ConsentResponse.class, LansforsakringarConstants.BodyValues.EMPTY_BODY);
    }

    public ConsentResponse authorizeConsent(String consentId) {

        return createRequest(
                        new URL(LansforsakringarConstants.Urls.CONSENT_PROVIDED)
                                .parameter(LansforsakringarConstants.IdTags.CONSENT_ID, consentId))
                .header(
                        LansforsakringarConstants.HeaderKeys.PSU_ID,
                        getCredentials().getField(Field.Key.USERNAME))
                .header(
                        LansforsakringarConstants.HeaderKeys.PSU_ID_TYPE,
                        LansforsakringarConstants.HeaderValues.PSU_ID_TYPE)
                .header(LansforsakringarConstants.HeaderKeys.TPP_REDIRECT_URI, getRedirectUrl())
                .header(LansforsakringarConstants.HeaderKeys.TPP_EXPLICIT_AUTH_PREFERRED, false)
                .post(ConsentResponse.class, LansforsakringarConstants.BodyValues.EMPTY_BODY);
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

        return client.request(tokenUrl)
                .header(HeaderKeys.X_REQUEST_ID, UUID.randomUUID())
                .header(HeaderKeys.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                .header(HeaderKeys.CACHE_CONTROL, HeaderValues.NO_CACHE)
                .accept(MediaType.APPLICATION_JSON)
                .body(form)
                .post(AuthenticateResponse.class)
                .toTinkToken();
    }

    public Collection<TransactionalAccount> getAccounts() {
        return createRequestInSession(Urls.GET_ACCOUNTS)
                .header(HeaderKeys.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED)
                .header(HeaderKeys.CONSENT_ID, persistentStorage.get(StorageKeys.CONSENT_ID))
                .header(HeaderKeys.CACHE_CONTROL, HeaderValues.NO_CACHE)
                .queryParam(QueryKeys.WITH_BALANCE, QueryValues.TRUE)
                .get(GetAccountsResponse.class)
                .toTinkAccounts();
    }

    public TransactionKeyPaginatorResponse<String> getTransactionsForKey(String key) {
        return createRequestInSession(Urls.BASE_API_URL + key)
                .header(HeaderKeys.CONSENT_ID, persistentStorage.get(StorageKeys.CONSENT_ID))
                .queryParam(QueryKeys.BOOKING_STATUS, QueryValues.BOTH)
                .get(GetTransactionsResponse.class);
    }

    public TransactionKeyPaginatorResponse<String> getTransactionsForAccount(
            TransactionalAccount account) {
        final URL url =
                new URL(Urls.GET_TRANSACTIONS)
                        .parameter(IdTags.ACCOUNT_ID, account.getApiIdentifier());

        return createRequestInSession(url)
                .header(HeaderKeys.CONSENT_ID, persistentStorage.get(StorageKeys.CONSENT_ID))
                .queryParam(
                        QueryKeys.DATE_FROM,
                        ThreadSafeDateFormat.FORMATTER_DAILY.format(new Date()))
                .queryParam(QueryKeys.BOOKING_STATUS, QueryValues.BOTH)
                .get(GetTransactionsResponse.class);
    }

    private OAuth2Token getTokenFromStorage() {
        return sessionStorage
                .get(StorageKeys.ACCESS_TOKEN, OAuth2Token.class)
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

    public void signPayment(String paymentId) {
        createRequestInSession(new URL(Urls.SIGN_PAYMENT).parameter(IdTags.PAYMENT_ID, paymentId))
                .post(AuthorizePaymentResponse.class);
    }
}
