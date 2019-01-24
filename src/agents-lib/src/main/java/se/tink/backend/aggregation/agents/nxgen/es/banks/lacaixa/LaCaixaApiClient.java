package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa;

import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.authenticator.rpc.LoginRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.authenticator.rpc.LoginResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.authenticator.rpc.SessionRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.authenticator.rpc.SessionResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.creditcard.rpc.CardTransactionsRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.creditcard.rpc.CardTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.creditcard.rpc.GenericCardsRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.creditcard.rpc.GenericCardsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.investments.rpc.EngagementResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.investments.rpc.PositionDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.investments.rpc.PositionValuesResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.transactionalaccount.rpc.AccountTransactionResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.transactionalaccount.rpc.ListAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.transactionalaccount.rpc.UserDataRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.transactionalaccount.rpc.UserDataResponse;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;

public class LaCaixaApiClient {

    private final TinkHttpClient client;

    public LaCaixaApiClient(TinkHttpClient client) {

        this.client = client;
    }

    public SessionResponse initializeSession() {

        SessionRequest request = new SessionRequest(
                LaCaixaConstants.DefaultRequestParams.LANGUAGE_EN,
                LaCaixaConstants.DefaultRequestParams.ORIGIN,
                LaCaixaConstants.DefaultRequestParams.CHANNEL,
                LaCaixaConstants.DefaultRequestParams.INSTALLATION_ID
        );

        return createRequest(LaCaixaConstants.Urls.INIT_LOGIN)
                .post(SessionResponse.class, request);
    }

    public LoginResponse login(LoginRequest loginRequest) throws LoginException {

        try {

            return createRequest(LaCaixaConstants.Urls.SUBMIT_LOGIN)
                    .post(LoginResponse.class, loginRequest);

        } catch (HttpResponseException e) {
            int statusCode = e.getResponse().getStatus();

            if (statusCode == LaCaixaConstants.StatusCodes.INCORRECT_USERNAME_PASSWORD) {
                throw LoginError.INCORRECT_CREDENTIALS.exception();
            }

            throw e;
        }
    }

    public void logout() {
        createRequest(LaCaixaConstants.Urls.LOGOUT)
                .post();
    }

    public ListAccountsResponse fetchAccountList() {

        return createRequest(LaCaixaConstants.Urls.FETCH_MAIN_ACCOUNT)
                .get(ListAccountsResponse.class);
    }

    public UserDataResponse fetchUserData() {

        UserDataRequest request = new UserDataRequest(LaCaixaConstants.UserData.FULL_HOLDER_NAME);

        return createRequest(LaCaixaConstants.Urls.FETCH_USER_DATA)
                .post(UserDataResponse.class, request);
    }

    public AccountTransactionResponse fetchNextAccountTransactions(String accountReference, boolean fromBegin) {

        return createRequest(LaCaixaConstants.Urls.FETCH_ACCOUNT_TRANSACTION)
                .queryParam(LaCaixaConstants.QueryParams.FROM_BEGIN, Boolean.toString(fromBegin))
                .queryParam(LaCaixaConstants.QueryParams.ACCOUNT_NUMBER, accountReference)
                .get(AccountTransactionResponse.class);
    }

    public EngagementResponse fetchEngagements() {
        return createRequest(LaCaixaConstants.Urls.FETCH_ENGAGEMENTS)
                .queryParam(LaCaixaConstants.QueryParams.ZERO_BALANCE_CONTRACTS,
                        LaCaixaConstants.DefaultRequestParams.ZERO_BALANCE_CONTRACTS)
                .queryParam(LaCaixaConstants.QueryParams.GLOBAL_POSITION_TYPE,
                        LaCaixaConstants.DefaultRequestParams.GLOBAL_POSITION_TYPE)
                .get(EngagementResponse.class);
    }

    public PositionValuesResponse fetchDepositList() {
        return createRequest(LaCaixaConstants.Urls.FETCH_DEPOSITS_LIST)
                .get(PositionValuesResponse.class);
    }

    public PositionValuesResponse fetchDeposit(String depositId, boolean moreData) {
        return createRequest(LaCaixaConstants.Urls.FETCH_DEPOSITS_LIST)
                .queryParam(LaCaixaConstants.QueryParams.DEPOSIT_ID, depositId)
                .queryParam(LaCaixaConstants.QueryParams.MORE_DATA, Boolean.toString(moreData))
                .get(PositionValuesResponse.class);
    }

    public PositionDetailsResponse fetchPositionDetails(String depositId, String depositContentId) {
        return createRequest(LaCaixaConstants.Urls.FETCH_DEPOSIT_DETAILS)
                .queryParam(LaCaixaConstants.QueryParams.DEPOSIT_CONTENT_ID, depositContentId)
                .queryParam(LaCaixaConstants.QueryParams.DEPOSIT_ID, depositId)
                .get(PositionDetailsResponse.class);
    }

    public GenericCardsResponse fetchCards() {
        return createRequest(LaCaixaConstants.Urls.FETCH_CARDS)
                .body(new GenericCardsRequest(true, LaCaixaConstants.DefaultRequestParams.NUM_CARDS))
                .post(GenericCardsResponse.class);
    }

    public CardTransactionsResponse fetchCardTransactions(String cardId, boolean start) {
        return createRequest(LaCaixaConstants.Urls.FETCH_CARD_TRANSACTIONS)
                .body(new CardTransactionsRequest(cardId, start))
                .post(CardTransactionsResponse.class);
    }

    public boolean isAlive() {

        try {

            createRequest(LaCaixaConstants.Urls.KEEP_ALIVE).get(HttpResponse.class);
        } catch (HttpResponseException e) {

            return false;
        }

        return true;
    }

    private RequestBuilder createRequest(URL url) {

        return client
                .request(url)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .accept(MediaType.APPLICATION_JSON_TYPE);
    }
}
