package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar;

import java.util.Collection;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import javax.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.authenticator.rpc.AuthenticateForm;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.authenticator.rpc.AuthenticateResponse;
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
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.date.ThreadSafeDateFormat;

public final class LansforsakringarApiClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(LansforsakringarApiClient.class);

    private final TinkHttpClient client;
    private final SessionStorage sessionStorage;
    private LansforsakringarConfiguration configuration;

    LansforsakringarApiClient(TinkHttpClient client, SessionStorage sessionStorage) {
        this.client = client;
        this.sessionStorage = sessionStorage;
    }

    public LansforsakringarConfiguration getConfiguration() {
        return Optional.ofNullable(configuration)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    public void setConfiguration(LansforsakringarConfiguration configuration) {
        this.configuration = configuration;
    }

    private RequestBuilder createRequest(URL url) {
        return client.request(url)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .header(HeaderKeys.PSU_IP_ADDRESS, HeaderValues.PSU_IP_ADDRESS)
                .header(HeaderKeys.PSU_USER_AGENT, HeaderValues.PSU_USER_AGENT)
                .header(HeaderKeys.X_REQUEST_ID, UUID.randomUUID());
    }

    private RequestBuilder createRequestInSession(URL url) {
        return createRequest(url).addBearerToken(getToken());
    }

    private RequestBuilder createRequestInSession(String url) {
        return createRequestInSession(new URL(url));
    }

    public OAuth2Token authenticate() {
        final String clientId = getConfiguration().getClientId();
        final String clientSecret = getConfiguration().getClientSecret();

        final AuthenticateForm form =
                AuthenticateForm.builder()
                        .setClientId(clientId)
                        .setClientSecret(clientSecret)
                        .setGrantType(FormValues.CLIENT_CREDENTIALS)
                        .build();

        return client.request(Urls.AUTHENTICATE)
                .body(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                .post(AuthenticateResponse.class)
                .toTinkToken();
    }

    public Collection<TransactionalAccount> getAccounts() {
        return createRequestInSession(Urls.GET_ACCOUNTS)
                .header(HeaderKeys.CONSENT_ID, configuration.getConsentId())
                .queryParam(QueryKeys.WITH_BALANCE, QueryValues.TRUE)
                .get(GetAccountsResponse.class)
                .toTinkAccounts();
    }

    public TransactionKeyPaginatorResponse<String> getTransactionsForKey(String key) {
        return createRequestInSession(Urls.BASE_URL + key)
                .header(HeaderKeys.CONSENT_ID, configuration.getConsentId())
                .queryParam(QueryKeys.BOOKING_STATUS, QueryValues.BOTH)
                .get(GetTransactionsResponse.class);
    }

    public TransactionKeyPaginatorResponse<String> getTransactionsForAccount(
            TransactionalAccount account) {
        final URL url =
                new URL(Urls.GET_TRANSACTIONS)
                        .parameter(IdTags.ACCOUNT_ID, account.getApiIdentifier());

        return createRequestInSession(url)
                .header(HeaderKeys.CONSENT_ID, configuration.getConsentId())
                .queryParam(
                        QueryKeys.DATE_FROM,
                        ThreadSafeDateFormat.FORMATTER_DAILY.format(new Date()))
                .queryParam(QueryKeys.BOOKING_STATUS, QueryValues.BOTH)
                .get(GetTransactionsResponse.class);
    }

    private OAuth2Token getToken() {
        return sessionStorage
                .get(StorageKeys.ACCESS_TOKEN, OAuth2Token.class)
                .orElseThrow(
                        () -> {
                            LOGGER.warn("Failed to retrieve access token.");
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
                .header(HeaderKeys.TPP_REDIRECT_URI, configuration.getRedirectUrl())
                .post(DomesticPaymentResponse.class, domesticPaymentRequest);
    }

    public CrossBorderPaymentResponse createCrossBorderPayment(
            CrossBorderPaymentRequest crossBorderPaymentRequest) {
        return createRequestInSession(
                        new URL(Urls.CREATE_PAYMENT)
                                .parameter(
                                        IdTags.PAYMENT_TYPE,
                                        PaymentTypes.CROSS_BORDER_CREDIT_TRANSFERS))
                .header(HeaderKeys.TPP_REDIRECT_URI, configuration.getRedirectUrl())
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
