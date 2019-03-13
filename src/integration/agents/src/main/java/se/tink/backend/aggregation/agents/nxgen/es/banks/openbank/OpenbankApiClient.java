package se.tink.backend.aggregation.agents.nxgen.es.banks.openbank;

import io.vavr.collection.List;
import io.vavr.control.Option;
import java.util.Date;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.authenticator.rpc.LoginRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.authenticator.rpc.LoginResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.authenticator.rpc.LogoutResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.fetcher.creditcard.rpc.CardTransactionsRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.fetcher.creditcard.rpc.CardTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.fetcher.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.fetcher.entities.CardEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.fetcher.rpc.UserDataResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.fetcher.transactionalaccount.rpc.AccountTransactionDetailsRequestQueryParams;
import se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.fetcher.transactionalaccount.rpc.AccountTransactionsRequestQueryParams;
import se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.fetcher.transactionalaccount.rpc.AccountTransactionsResponse;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.date.ThreadSafeDateFormat;

public final class OpenbankApiClient {
    private final TinkHttpClient client;
    private final SessionStorage sessionStorage;

    public OpenbankApiClient(TinkHttpClient client, SessionStorage sessionStorage) {
        this.client = client;
        this.sessionStorage = sessionStorage;
    }

    private RequestBuilder createRequest(URL url) {
        return client.request(url)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON);
    }

    private RequestBuilder createRequestInSession(URL url) {
        return createRequest(url)
                .header(
                        OpenbankConstants.Headers.AUTH_TOKEN,
                        sessionStorage.get(OpenbankConstants.Storage.AUTH_TOKEN));
    }

    private RequestBuilder createTransactionsRequestInSession(
            AccountTransactionsRequestQueryParams queryParams) {
        return createRequestInSession(OpenbankConstants.Urls.ACCOUNT_TRANSACTIONS)
                .queryParam(
                        OpenbankConstants.QueryParams.PRODUCT_CODE, queryParams.getProductCode())
                .queryParam(
                        OpenbankConstants.QueryParams.CONTRACT_NUMBER,
                        queryParams.getContractNumber());
    }

    public LoginResponse login(LoginRequest loginRequest) {
        return createRequest(OpenbankConstants.Urls.LOGIN).post(LoginResponse.class, loginRequest);
    }

    public void keepAlive() {
        createRequestInSession(OpenbankConstants.Urls.KEEP_ALIVE).get(String.class);
    }

    public LogoutResponse logout() {
        return createRequestInSession(OpenbankConstants.Urls.LOGOUT).post(LogoutResponse.class);
    }

    public UserDataResponse getUserData() {
        return createRequestInSession(OpenbankConstants.Urls.USER_DATA).get(UserDataResponse.class);
    }

    public List<AccountEntity> getAccounts() {
        return Option.of(getUserData()).map(UserDataResponse::getAccounts).getOrElse(List::empty);
    }

    public AccountTransactionsResponse getTransactions(
            AccountTransactionsRequestQueryParams queryParams) {
        return createTransactionsRequestInSession(queryParams)
                .get(AccountTransactionsResponse.class);
    }

    public AccountTransactionsResponse getTransactionsForNextUrl(URL nextUrl) {
        return createRequestInSession(nextUrl).get(AccountTransactionsResponse.class);
    }

    public AccountTransactionsResponse getTransactionsFor(
            AccountTransactionsRequestQueryParams queryParams, Date fromDate, Date toDate) {
        return createTransactionsRequestInSession(queryParams)
                .queryParam(
                        OpenbankConstants.QueryParams.FROM_DATE,
                        ThreadSafeDateFormat.FORMATTER_DAILY.format(fromDate))
                .queryParam(
                        OpenbankConstants.QueryParams.TO_DATE,
                        ThreadSafeDateFormat.FORMATTER_DAILY.format(toDate))
                .get(AccountTransactionsResponse.class);
    }

    public AccountTransactionsResponse getTransactionDetails(
            AccountTransactionDetailsRequestQueryParams queryParams) {
        return client.request(OpenbankConstants.Urls.ACCOUNT_TRANSACTIONS)
                .queryParam(
                        OpenbankConstants.QueryParams.CONNECTING_PRODUCT_CODE,
                        queryParams.getProductCodeOld())
                .queryParam(
                        OpenbankConstants.QueryParams.CONNECTING_CONTRACT_NUMBER,
                        queryParams.getContractNumberOld())
                .queryParam(
                        OpenbankConstants.QueryParams.PRODUCT_CODE, queryParams.getProductCodeNew())
                .queryParam(
                        OpenbankConstants.QueryParams.CONTRACT_NUMBER,
                        queryParams.getContractNumberNew())
                .queryParam(
                        OpenbankConstants.QueryParams.LANGUAGE_CODE,
                        OpenbankConstants.Codes.LANGUAGE_CODE)
                .queryParam(
                        OpenbankConstants.QueryParams.CURRENCY_CODE,
                        OpenbankConstants.Codes.CURRENCY_CODE)
                .queryParam(
                        OpenbankConstants.QueryParams.PROCEDURE_CODE,
                        OpenbankConstants.Codes.PROCEDURE_CODE)
                .queryParam(
                        OpenbankConstants.QueryParams.MOVEMENT_OF_THE_DAY_INDEX,
                        queryParams.getMovementOfTheDayIndex())
                .queryParam(OpenbankConstants.QueryParams.DATE_NOTED, queryParams.getDateNoted())
                .queryParam(OpenbankConstants.QueryParams.SITUATION_ID, "1")
                .get(AccountTransactionsResponse.class);
    }

    public List<CardEntity> getCards() {
        return Option.of(getUserData()).map(UserDataResponse::getCards).getOrElse(List::empty);
    }

    public CardTransactionsResponse getCardTransactions(CardTransactionsRequest request) {
        return createRequestInSession(OpenbankConstants.Urls.CARD_TRANSACTIONS)
                .body(request)
                .post(CardTransactionsResponse.class);
    }
}
