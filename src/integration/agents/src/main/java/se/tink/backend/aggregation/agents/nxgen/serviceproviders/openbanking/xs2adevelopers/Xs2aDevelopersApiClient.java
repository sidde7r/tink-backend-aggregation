package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.utils.CryptoUtils.getCodeChallenge;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.utils.CryptoUtils.getCodeVerifier;

import java.util.Date;
import java.util.UUID;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersConstants.ApiServices;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersConstants.IdTags;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.authenticator.rpc.GetTokenForm;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.authenticator.rpc.GetTokenResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.authenticator.rpc.PostConsentBody;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.authenticator.rpc.PostConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.configuration.Xs2aDevelopersConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.executor.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.executor.payment.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.executor.payment.rpc.GetPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.fetcher.transactionalaccount.rpc.GetAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.fetcher.transactionalaccount.rpc.GetBalanceResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.fetcher.transactionalaccount.rpc.GetTransactionsResponse;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.date.ThreadSafeDateFormat;

public class Xs2aDevelopersApiClient {

    protected final TinkHttpClient client;
    private final PersistentStorage persistentStorage;
    private final Xs2aDevelopersConfiguration configuration;
    private final String redirectUrl;

    public Xs2aDevelopersApiClient(
            TinkHttpClient client,
            PersistentStorage persistentStorage,
            AgentConfiguration<Xs2aDevelopersConfiguration> agentConfiguration) {
        this.client = client;
        this.persistentStorage = persistentStorage;
        this.configuration = agentConfiguration.getClientConfiguration();
        this.redirectUrl = agentConfiguration.getRedirectUrl();
    }

    private RequestBuilder createRequest(URL url) {
        return client.request(url)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .type(MediaType.APPLICATION_JSON);
    }

    private RequestBuilder createRequestInSession(URL url) {
        final OAuth2Token authToken = getTokenFromStorage(StorageKeys.OAUTH_TOKEN);
        return createRequest(url).addBearerToken(authToken);
    }

    private RequestBuilder createFetchingRequest(URL url) {
        return createRequestInSession(url)
                .header(HeaderKeys.CONSENT_ID, getConsentIdFromStorage())
                .header(HeaderKeys.X_REQUEST_ID, UUID.randomUUID());
    }

    private String getConsentIdFromStorage() {
        return persistentStorage.get(StorageKeys.CONSENT_ID);
    }

    private OAuth2Token getTokenFromStorage(String key) {
        return persistentStorage
                .get(key, OAuth2Token.class)
                .orElseThrow(
                        () -> new IllegalStateException(SessionError.SESSION_EXPIRED.exception()));
    }

    public PostConsentResponse createConsent(PostConsentBody postConsentBody) {
        return createRequest(new URL(configuration.getBaseUrl() + ApiServices.POST_CONSENT))
                .header(HeaderKeys.TPP_REDIRECT_URI, redirectUrl)
                .header(HeaderKeys.PSU_IP_ADDRESS, QueryValues.PSU_IP_ADDRESS)
                .header(HeaderKeys.X_REQUEST_ID, UUID.randomUUID())
                .body(postConsentBody)
                .post(PostConsentResponse.class);
    }

    public URL buildAuthorizeUrl(String state, String scope, String href) {
        String code = getCodeVerifier();
        persistentStorage.put(StorageKeys.CODE_VERIFIER, code);

        return new URL(href)
                .queryParam(QueryKeys.STATE, state)
                .queryParam(QueryKeys.REDIRECT_URI, redirectUrl)
                .queryParam(QueryKeys.CLIENT_ID, configuration.getClientId())
                .queryParam(QueryKeys.SCOPE, scope)
                .queryParam(QueryKeys.CODE_CHALLENGE, getCodeChallenge(code))
                .queryParam(QueryKeys.RESPONSE_TYPE, QueryValues.CODE)
                .queryParam(QueryKeys.CODE_CHALLENGE_TYPE_M, QueryValues.CODE_CHALLENGE_TYPE);
    }

    public GetTokenResponse getToken(GetTokenForm getTokenForm) {
        return createRequest(new URL(configuration.getBaseUrl() + ApiServices.TOKEN))
                .body(getTokenForm, MediaType.APPLICATION_FORM_URLENCODED)
                .post(GetTokenResponse.class);
    }

    public GetAccountsResponse getAccounts() {
        return createFetchingRequest(new URL(configuration.getBaseUrl() + ApiServices.GET_ACCOUNTS))
                .get(GetAccountsResponse.class);
    }

    public GetBalanceResponse getBalance(AccountEntity account) {
        return createFetchingRequest(
                        new URL(configuration.getBaseUrl() + ApiServices.GET_BALANCES)
                                .parameter(IdTags.ACCOUNT_ID, account.getResourceId()))
                .get(GetBalanceResponse.class);
    }

    public GetTransactionsResponse getTransactions(
            TransactionalAccount account, Date fromDate, Date toDate) {
        return createFetchingRequest(
                        new URL(configuration.getBaseUrl() + ApiServices.GET_TRANSACTIONS)
                                .parameter(IdTags.ACCOUNT_ID, account.getApiIdentifier()))
                .queryParam(
                        QueryKeys.DATE_FROM, ThreadSafeDateFormat.FORMATTER_DAILY.format(fromDate))
                .queryParam(QueryKeys.DATE_TO, ThreadSafeDateFormat.FORMATTER_DAILY.format(toDate))
                .queryParam(QueryKeys.BOOKING_STATUS, QueryValues.BOTH)
                .get(GetTransactionsResponse.class);
    }

    public GetTransactionsResponse getCreditTransactions(
            CreditCardAccount account, Date fromDate, Date toDate) {
        return createFetchingRequest(
                        new URL(configuration.getBaseUrl() + ApiServices.GET_TRANSACTIONS)
                                .parameter(IdTags.ACCOUNT_ID, account.getApiIdentifier()))
                .queryParam(
                        QueryKeys.DATE_FROM, ThreadSafeDateFormat.FORMATTER_DAILY.format(fromDate))
                .queryParam(QueryKeys.DATE_TO, ThreadSafeDateFormat.FORMATTER_DAILY.format(toDate))
                .queryParam(QueryKeys.BOOKING_STATUS, QueryValues.BOTH)
                .get(GetTransactionsResponse.class);
    }

    public CreatePaymentResponse createPayment(CreatePaymentRequest createPaymentRequest) {
        return createRequest(new URL(configuration.getBaseUrl() + ApiServices.CREATE_PAYMENT))
                .header(HeaderKeys.TPP_REDIRECT_URI, redirectUrl)
                .header(HeaderKeys.PSU_IP_ADDRESS, QueryValues.PSU_IP_ADDRESS)
                .header(HeaderKeys.X_REQUEST_ID, UUID.randomUUID())
                .body(createPaymentRequest)
                .post(CreatePaymentResponse.class);
    }

    public GetPaymentResponse getPayment(String paymentId) {
        return createRequest(
                        new URL(configuration.getBaseUrl() + ApiServices.GET_PAYMENT)
                                .parameter(IdTags.PAYMENT_ID, paymentId))
                .header(
                        HeaderKeys.AUTHORIZATION,
                        getTokenFromStorage(StorageKeys.PIS_TOKEN).getAccessToken())
                .header(HeaderKeys.X_REQUEST_ID, UUID.randomUUID())
                .get(GetPaymentResponse.class);
    }
}
