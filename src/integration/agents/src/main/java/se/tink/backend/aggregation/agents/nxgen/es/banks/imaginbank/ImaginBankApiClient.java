package se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank;

import static se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.ImaginBankConstants.Storage.APP_INSTALLATION_ID;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.ImaginBankConstants.Storage.USER_AGENT_ID;

import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import javax.ws.rs.core.MediaType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.ImaginBankConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.ImaginBankConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.authenticator.rpc.EnrollmentResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.authenticator.rpc.EnrollmentScaRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.authenticator.rpc.ImaginSessionRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.authenticator.rpc.LoginRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.authenticator.rpc.LoginResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.authenticator.rpc.SessionRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.authenticator.rpc.SessionResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.fetcher.creditcard.rpc.CardTransactionsRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.fetcher.creditcard.rpc.CardTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.fetcher.creditcard.rpc.CardsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.fetcher.identitydata.rpc.UserDataRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.fetcher.identitydata.rpc.UserDataResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.fetcher.transactionalaccount.entities.HolderEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.fetcher.transactionalaccount.rpc.AccountTransactionResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.fetcher.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.fetcher.transactionalaccount.rpc.ListHoldersResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.rpc.ImaginBankErrorResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.utils.ImaginBankRegistrationDataGenerator;
import se.tink.backend.aggregation.nxgen.core.account.entity.Party;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@Slf4j
public class ImaginBankApiClient {
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final TinkHttpClient client;
    private PersistentStorage persistentStorage;
    private ImaginBankSessionStorage imaginBankSessionStorage;

    public ImaginBankApiClient(
            TinkHttpClient client,
            PersistentStorage persistentStorage,
            ImaginBankSessionStorage imaginBankSessionStorage) {
        this.client = client;
        this.persistentStorage = persistentStorage;
        this.imaginBankSessionStorage = imaginBankSessionStorage;
    }

    public SessionResponse initializeSession(String username) {

        ImaginSessionRequest request =
                new ImaginSessionRequest(username, new SessionRequest(retrieveAppInstallationId()));

        return createPostRequest(ImaginBankConstants.Urls.INIT_LOGIN)
                .post(SessionResponse.class, request);
    }

    public LoginResponse login(LoginRequest loginRequest) throws LoginException {
        try {
            return createPostRequest(ImaginBankConstants.Urls.SUBMIT_LOGIN)
                    .post(LoginResponse.class, loginRequest);

        } catch (HttpResponseException e) {
            HttpResponse response = e.getResponse();
            ImaginBankErrorResponse errorResponse = response.getBody(ImaginBankErrorResponse.class);
            if (response.getStatus() == HttpStatus.SC_CONFLICT) {
                if (errorResponse.isAccountBlocked()) {
                    throw AuthorizationError.ACCOUNT_BLOCKED.exception();
                } else if (errorResponse.isIdentificationIncorrect()) {
                    throw LoginError.INCORRECT_CREDENTIALS.exception(e);
                } else if (errorResponse.isAccessBanned()) {
                    throw LoginError.NOT_SUPPORTED.exception();
                }
            }
            log.info(
                    "Unknown error code {} with message {}",
                    errorResponse.getCode(),
                    errorResponse.getMessage());
            throw LoginError.DEFAULT_MESSAGE.exception();
        }
    }

    public AccountsResponse fetchAccounts() {
        return createGetRequest(
                        ImaginBankConstants.Urls.FETCH_ACCOUNTS.queryParam(
                                ImaginBankConstants.QueryParams.FROM_BEGIN, "true"))
                .get(AccountsResponse.class);
    }

    public AccountTransactionResponse fetchNextAccountTransactions(
            String accountReference, boolean fromBegin) {

        return createGetRequest(ImaginBankConstants.Urls.FETCH_ACCOUNT_TRANSACTION)
                .queryParam(ImaginBankConstants.QueryParams.FROM_BEGIN, Boolean.toString(fromBegin))
                .queryParam(ImaginBankConstants.QueryParams.ACCOUNT_NUMBER, accountReference)
                .get(AccountTransactionResponse.class);
    }

    public void initiateCardFetching() {
        try {
            String initCardsResponse =
                    createPostRequest(ImaginBankConstants.Urls.INITIATE_CARD_FETCHING)
                            .post(String.class, "{}");
            logger.info("Initiated card fetching {}", initCardsResponse);
        } catch (HttpResponseException e) {
            HttpResponse response = e.getResponse();
            ImaginBankErrorResponse errorResponse = response.getBody(ImaginBankErrorResponse.class);
            logger.info(
                    "Failed to initiate card fetching with error code {} and message {}",
                    errorResponse.getCode(),
                    errorResponse.getMessage());
            if (response.getStatus() == HttpStatus.SC_CONFLICT
                    && errorResponse.isCurrentlyUnavailable()) {
                throw BankServiceError.BANK_SIDE_FAILURE.exception(e);
            }
            throw e;
        }
    }

    public CardsResponse fetchCards() {
        return createGetRequest(
                        ImaginBankConstants.Urls.FETCH_CARDS
                                .queryParam(
                                        ImaginBankConstants.QueryParams.INITIALIZED_BOXES,
                                        ImaginBankConstants.QueryParams.INITIALIZED_BOXES_VALUE)
                                .queryParam(
                                        ImaginBankConstants.QueryParams.CARD_STATUS,
                                        ImaginBankConstants.QueryParams.CARD_STATUS_VALUE)
                                .queryParam(
                                        ImaginBankConstants.QueryParams.MORE_DATA,
                                        ImaginBankConstants.QueryParams.MORE_DATA_VALUE)
                                .queryParam(
                                        ImaginBankConstants.QueryParams.PROFILE,
                                        ImaginBankConstants.QueryParams.PROFILE_VALUE))
                .get(CardsResponse.class);
    }

    public List<Party> fetchPartiesForAccount(AccountEntity accountEntity) {
        ListHoldersResponse listHoldersResponse =
                fetchHolderList(accountEntity.getIdentifiers().getAccountReference());
        return HolderEntity.toParties(listHoldersResponse);
    }

    private ListHoldersResponse fetchHolderList(String accountReference) {
        return createGetRequest(ImaginBankConstants.Urls.HOLDERS_LIST)
                .queryParam(ImaginBankConstants.QueryParams.FROM_BEGIN, "true")
                .queryParam(ImaginBankConstants.QueryParams.ACCOUNT_NUMBER, accountReference)
                .get(ListHoldersResponse.class);
    }

    public CardTransactionsResponse fetchCardTransactions(
            String cardKey, LocalDate fromDate, LocalDate toDate, boolean moreData) {
        CardTransactionsRequest request =
                CardTransactionsRequest.createCardTransactionsRequest(
                        moreData, cardKey, fromDate, toDate);

        return createPostRequest(ImaginBankConstants.Urls.FETCH_CARD_TRANSACTIONS)
                .post(CardTransactionsResponse.class, request);
    }

    public void logout() {
        createPostRequest(ImaginBankConstants.Urls.LOGOUT).post();
    }

    public boolean isAlive() {

        try {

            createGetRequest(ImaginBankConstants.Urls.KEEP_ALIVE).get(HttpResponse.class);
        } catch (HttpResponseException e) {

            return false;
        }

        return true;
    }

    public UserDataResponse fetchDni() {
        UserDataRequest request = new UserDataRequest(ImaginBankConstants.IdentityData.DNI);

        return createPostRequest(ImaginBankConstants.Urls.USER_DATA)
                .post(UserDataResponse.class, request);
    }

    public EnrollmentResponse initEnrollment() {
        return createGetRequest(ImaginBankConstants.Urls.INIT_ENROLLMENT)
                .get(EnrollmentResponse.class);
    }

    public EnrollmentResponse doPasswordEnrollment(String code) {
        EnrollmentScaRequest enrollmentScaRequest = new EnrollmentScaRequest(code);
        return createPostRequest(Urls.SCA_ENROLLMENT)
                .post(EnrollmentResponse.class, enrollmentScaRequest);
    }

    public EnrollmentResponse doOtpEnrollment(String code) {
        EnrollmentScaRequest enrollmentScaRequest = new EnrollmentScaRequest(code);
        return createPostRequest(Urls.SCA_ENROLLMENT_RESULT)
                .post(EnrollmentResponse.class, enrollmentScaRequest);
    }

    private RequestBuilder createPostRequest(URL url) {
        return client.request(url)
                .header(HeaderKeys.USER_AGENT, retrieveUserAgent())
                .header(HeaderKeys.X_REQUEST_ID, UUID.randomUUID().toString().toUpperCase())
                .type(MediaType.APPLICATION_JSON_TYPE)
                .acceptLanguage("en-us")
                .accept(MediaType.WILDCARD);
    }

    private RequestBuilder createGetRequest(URL url) {
        return client.request(url)
                .header(HeaderKeys.USER_AGENT, retrieveUserAgent())
                .header(HeaderKeys.X_REQUEST_ID, UUID.randomUUID().toString().toUpperCase())
                .acceptLanguage("en-us")
                .accept(MediaType.WILDCARD);
    }

    private String retrieveUserAgent() {
        String userAgent = persistentStorage.get(USER_AGENT_ID);
        if (userAgent == null) {
            userAgent =
                    ImaginBankRegistrationDataGenerator.generateUserAgent(
                            imaginBankSessionStorage.getUsername(), false);
            if (StringUtils.isNotEmpty(imaginBankSessionStorage.getUsername())) {
                persistentStorage.put(USER_AGENT_ID, userAgent);
            }
        }
        return userAgent;
    }

    private String retrieveAppInstallationId() {
        String appInstallationId = persistentStorage.get(APP_INSTALLATION_ID);
        if (appInstallationId == null) {
            appInstallationId =
                    ImaginBankRegistrationDataGenerator.generateAppInstallationId(
                            imaginBankSessionStorage.getUsername(), false);
            if (StringUtils.isNotEmpty(imaginBankSessionStorage.getUsername())) {
                persistentStorage.put(APP_INSTALLATION_ID, appInstallationId);
            }
        }
        return appInstallationId;
    }

    public TinkHttpClient getClient() {
        return client;
    }
}
