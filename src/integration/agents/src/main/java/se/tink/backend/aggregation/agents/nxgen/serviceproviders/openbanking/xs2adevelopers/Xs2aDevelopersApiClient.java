package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.utils.CryptoUtils.getCodeChallenge;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.utils.CryptoUtils.getCodeVerifier;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import javax.ws.rs.core.MediaType;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
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
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.configuration.Xs2aDevelopersProviderConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.executor.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.executor.payment.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.executor.payment.rpc.GetPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.fetcher.transactionalaccount.rpc.GetAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.fetcher.transactionalaccount.rpc.GetBalanceResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.fetcher.transactionalaccount.rpc.GetTransactionsResponse;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@Slf4j
public class Xs2aDevelopersApiClient {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    protected final TinkHttpClient client;
    private final PersistentStorage persistentStorage;
    private final Xs2aDevelopersProviderConfiguration configuration;
    private final boolean isManual;
    private final String userIp;
    private final RandomValueGenerator randomValueGenerator;

    public Xs2aDevelopersApiClient(
            TinkHttpClient client,
            PersistentStorage persistentStorage,
            Xs2aDevelopersProviderConfiguration configuration,
            boolean isManual,
            String userIp,
            RandomValueGenerator randomValueGenerator) {
        this.client = client;
        this.persistentStorage = persistentStorage;
        this.configuration = configuration;
        this.isManual = isManual;
        this.userIp = userIp;
        this.randomValueGenerator = randomValueGenerator;
    }

    protected RequestBuilder createRequest(URL url) {
        return client.request(url)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .type(MediaType.APPLICATION_JSON);
    }

    private RequestBuilder createRequestInSession(URL url) {
        final OAuth2Token authToken = getTokenFromStorage(StorageKeys.OAUTH_TOKEN);
        return createRequest(url).addBearerToken(authToken);
    }

    private RequestBuilder createFetchingRequest(URL url) {

        RequestBuilder requestBuilder =
                createRequestInSession(url)
                        .header(HeaderKeys.CONSENT_ID, getConsentIdFromStorage())
                        .header(HeaderKeys.X_REQUEST_ID, randomValueGenerator.getUUID());

        return isManual ? requestBuilder.header(HeaderKeys.PSU_IP_ADDRESS, userIp) : requestBuilder;
    }

    private String getConsentIdFromStorage() {
        return persistentStorage.get(StorageKeys.CONSENT_ID);
    }

    private OAuth2Token getTokenFromStorage(String key) {
        return persistentStorage
                .get(key, OAuth2Token.class)
                .orElseThrow(SessionError.SESSION_EXPIRED::exception);
    }

    public PostConsentResponse createConsent(PostConsentBody postConsentBody) {
        return createRequest(new URL(configuration.getBaseUrl() + ApiServices.POST_CONSENT))
                .header(HeaderKeys.TPP_REDIRECT_URI, configuration.getRedirectUrl())
                .header(HeaderKeys.PSU_IP_ADDRESS, userIp)
                .header(HeaderKeys.X_REQUEST_ID, randomValueGenerator.getUUID())
                .body(postConsentBody)
                .post(PostConsentResponse.class);
    }

    public URL buildAuthorizeUrl(String state, String scope, String href) {
        String code = getCodeVerifier();
        persistentStorage.put(StorageKeys.CODE_VERIFIER, code);

        return new URL(href)
                .queryParam(QueryKeys.STATE, state)
                .queryParam(QueryKeys.REDIRECT_URI, configuration.getRedirectUrl())
                .queryParam(QueryKeys.CLIENT_ID, configuration.getClientId())
                .queryParam(QueryKeys.SCOPE, scope)
                .queryParam(QueryKeys.CODE_CHALLENGE, getCodeChallenge(code))
                .queryParam(QueryKeys.RESPONSE_TYPE, QueryValues.CODE)
                .queryParam(QueryKeys.CODE_CHALLENGE_TYPE_M, QueryValues.CODE_CHALLENGE_TYPE);
    }

    public GetTokenResponse getToken(GetTokenForm getTokenForm) {
        try {
            return createRequest(new URL(configuration.getBaseUrl() + ApiServices.TOKEN))
                    .body(getTokenForm, MediaType.APPLICATION_FORM_URLENCODED)
                    .post(GetTokenResponse.class);
        } catch (HttpResponseException hre) {
            log.error("Error caught while getting/refreshing access token", hre);
            if (hre.getResponse().getStatus() == 500) {
                throw BankServiceError.BANK_SIDE_FAILURE.exception(hre);
            } else {
                throw hre;
            }
        }
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

    public List<? extends Transaction> getTransactions(
            TransactionalAccount account, LocalDate fromDate, LocalDate toDate) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        List<Transaction> fetchedTransactions = new LinkedList<>();
        Optional<URL> transactionFetchUrl =
                Optional.of(
                        new URL(configuration.getBaseUrl() + ApiServices.GET_TRANSACTIONS)
                                .parameter(IdTags.ACCOUNT_ID, account.getApiIdentifier()));
        do {
            GetTransactionsResponse response =
                    createFetchingRequest(transactionFetchUrl.get())
                            .queryParam(QueryKeys.DATE_FROM, dateFormatter.format(fromDate))
                            .queryParam(QueryKeys.DATE_TO, dateFormatter.format(toDate))
                            .queryParam(QueryKeys.BOOKING_STATUS, QueryValues.BOTH)
                            .get(GetTransactionsResponse.class);
            fetchedTransactions.addAll(response.toTinkTransactions());
            transactionFetchUrl = response.getTransactions().getLinks().getNext().map(URL::new);
        } while (transactionFetchUrl.isPresent());
        return fetchedTransactions;
    }

    public GetTransactionsResponse getTransactions(
            Account account, LocalDate dateFrom, LocalDate toDate) {
        URL transactionFetchUrl =
                new URL(configuration.getBaseUrl() + ApiServices.GET_TRANSACTIONS)
                        .parameter(IdTags.ACCOUNT_ID, account.getApiIdentifier());

        return createFetchingRequest(transactionFetchUrl)
                .queryParam(QueryKeys.DATE_FROM, DATE_FORMATTER.format(dateFrom))
                .queryParam(QueryKeys.DATE_TO, DATE_FORMATTER.format(toDate))
                .queryParam(QueryKeys.BOOKING_STATUS, QueryValues.BOTH)
                .get(GetTransactionsResponse.class);
    }

    public GetTransactionsResponse getTransactions(String nextLink) {
        return createFetchingRequest(new URL(nextLink)).get(GetTransactionsResponse.class);
    }

    public CreatePaymentResponse createPayment(CreatePaymentRequest createPaymentRequest) {
        return createRequest(new URL(configuration.getBaseUrl() + ApiServices.CREATE_PAYMENT))
                .header(HeaderKeys.TPP_REDIRECT_URI, configuration.getRedirectUrl())
                .header(HeaderKeys.PSU_IP_ADDRESS, userIp)
                .header(HeaderKeys.X_REQUEST_ID, randomValueGenerator.getUUID())
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
                .header(HeaderKeys.X_REQUEST_ID, randomValueGenerator.getUUID())
                .get(GetPaymentResponse.class);
    }
}
