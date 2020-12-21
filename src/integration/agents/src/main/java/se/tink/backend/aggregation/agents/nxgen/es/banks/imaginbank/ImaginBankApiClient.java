package se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank;

import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import javax.ws.rs.core.MediaType;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.authenticator.rpc.LoginRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.authenticator.rpc.LoginResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.authenticator.rpc.SessionRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.authenticator.rpc.SessionResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.fetcher.creditcard.rpc.CardTransactionsRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.fetcher.creditcard.rpc.CardTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.fetcher.creditcard.rpc.CardsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.fetcher.identitydata.rpc.UserDataRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.fetcher.identitydata.rpc.UserDataResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.fetcher.transactionalaccount.rpc.AccountTransactionResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.fetcher.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.rpc.ImaginBankErrorResponse;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.serialization.utils.SerializationUtils;

@Slf4j
public class ImaginBankApiClient {
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final TinkHttpClient client;

    public ImaginBankApiClient(TinkHttpClient client) {

        this.client = client;
    }

    public SessionResponse initializeSession() {

        SessionRequest request =
                new SessionRequest(
                        ImaginBankConstants.DefaultRequestParams.LANGUAGE_EN,
                        ImaginBankConstants.DefaultRequestParams.ORIGIN,
                        ImaginBankConstants.DefaultRequestParams.CHANNEL,
                        ImaginBankConstants.DefaultRequestParams.INSTALLATION_ID,
                        ImaginBankConstants.DefaultRequestParams.VIRTUAL_KEYBOARD);

        return createRequest(ImaginBankConstants.Urls.INIT_LOGIN)
                .post(SessionResponse.class, request);
    }

    public LoginResponse login(LoginRequest loginRequest) throws LoginException {
        try {
            return createRequest(ImaginBankConstants.Urls.SUBMIT_LOGIN)
                    .post(LoginResponse.class, loginRequest);

        } catch (HttpResponseException e) {
            HttpResponse response = e.getResponse();
            ImaginBankErrorResponse errorResponse = response.getBody(ImaginBankErrorResponse.class);
            if (response.getStatus() == HttpStatus.SC_CONFLICT) {
                if (errorResponse.isAccountBlocked()) {
                    throw AuthorizationError.ACCOUNT_BLOCKED.exception();
                } else if (errorResponse.isIdentificationIncorrect()) {
                    throw LoginError.INCORRECT_CREDENTIALS.exception(e);
                }
            }
            log.info(
                    "Unknown error code {} with message {}",
                    errorResponse.getCode(),
                    errorResponse.getMessage());
            throw LoginError.DEFAULT_MESSAGE.exception();
        }
    }

    public AccountsResponse fetchAccounts(boolean fromBegin) {

        String accountsResponseRaw =
                createRequest(
                                ImaginBankConstants.Urls.FETCH_ACCOUNTS.queryParam(
                                        ImaginBankConstants.QueryParams.FROM_BEGIN,
                                        String.valueOf(fromBegin)))
                        .get(String.class);

        AccountsResponse accountsResponse =
                SerializationUtils.deserializeFromString(
                        accountsResponseRaw, AccountsResponse.class);

        // currently imagin bank only allows one account for a customer, log if we get more
        if (accountsResponse != null && accountsResponse.getNumberOfAccounts() > 1) {
            logger.warn(
                    "tag={} Got more than one account",
                    ImaginBankConstants.LogTags.MULTIPLE_ACCOUNTS);
        }

        return accountsResponse;
    }

    public AccountTransactionResponse fetchNextAccountTransactions(
            String accountReference, boolean fromBegin) {

        return createRequest(ImaginBankConstants.Urls.FETCH_ACCOUNT_TRANSACTION)
                .queryParam(ImaginBankConstants.QueryParams.FROM_BEGIN, Boolean.toString(fromBegin))
                .queryParam(ImaginBankConstants.QueryParams.ACCOUNT_NUMBER, accountReference)
                .get(AccountTransactionResponse.class);
    }

    public void initiateCardFetching() {
        try {
            String initCardsResponse =
                    createRequest(ImaginBankConstants.Urls.INITIATE_CARD_FETCHING)
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
        return createRequest(
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

    public CardTransactionsResponse fetchCardTransactions(
            String cardKey, LocalDate fromDate, LocalDate toDate, boolean moreData) {
        CardTransactionsRequest request =
                CardTransactionsRequest.createCardTransactionsRequest(
                        moreData, cardKey, fromDate, toDate);

        return createRequest(ImaginBankConstants.Urls.FETCH_CARD_TRANSACTIONS)
                .post(CardTransactionsResponse.class, request);
    }

    public void logout() {
        createRequest(ImaginBankConstants.Urls.LOGOUT).post();
    }

    public boolean isAlive() {

        try {

            createRequest(ImaginBankConstants.Urls.KEEP_ALIVE).get(HttpResponse.class);
        } catch (HttpResponseException e) {

            return false;
        }

        return true;
    }

    public UserDataResponse fetchDni() {
        UserDataRequest request = new UserDataRequest(ImaginBankConstants.IdentityData.DNI);

        return createRequest(ImaginBankConstants.Urls.USER_DATA)
                .post(UserDataResponse.class, request);
    }

    private RequestBuilder createRequest(URL url) {

        return client.request(url)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .accept(MediaType.APPLICATION_JSON_TYPE);
    }
}
