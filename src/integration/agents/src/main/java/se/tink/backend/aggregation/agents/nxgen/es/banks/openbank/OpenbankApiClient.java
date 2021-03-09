package se.tink.backend.aggregation.agents.nxgen.es.banks.openbank;

import io.vavr.collection.List;
import io.vavr.control.Option;
import java.util.Date;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.OpenbankConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.authenticator.rpc.LoginRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.authenticator.rpc.LoginResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.authenticator.rpc.LogoutResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.fetcher.creditcard.rpc.CardTransactionsRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.fetcher.creditcard.rpc.CardTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.fetcher.entities.CardEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.fetcher.rpc.IdentityResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.fetcher.rpc.UserDataResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.fetcher.transactionalaccount.rpc.AccountTransactionsRequestQueryParams;
import se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.fetcher.transactionalaccount.rpc.AccountTransactionsResponse;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.date.ThreadSafeDateFormat;

public class OpenbankApiClient {
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

    public String keepAlive() {
        return createRequestInSession(OpenbankConstants.Urls.KEEP_ALIVE).get(String.class);
    }

    public LogoutResponse logout() {
        return createRequestInSession(OpenbankConstants.Urls.LOGOUT).post(LogoutResponse.class);
    }

    public UserDataResponse fetchAccounts() {
        return createRequestInSession(OpenbankConstants.Urls.USER_DATA).get(UserDataResponse.class);
    }

    public AccountTransactionsResponse fetchTransactions(
            AccountTransactionsRequestQueryParams queryParams) {
        return createTransactionsRequestInSession(queryParams)
                .get(AccountTransactionsResponse.class);
    }

    public AccountTransactionsResponse fetchTransactionsForNextUrl(URL nextUrl) {
        return createRequestInSession(nextUrl).get(AccountTransactionsResponse.class);
    }

    public AccountTransactionsResponse fetchTransactionsFor(
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

    public List<CardEntity> fetchCards() {
        return Option.of(fetchAccounts()).map(UserDataResponse::getCards).getOrElse(List::empty);
    }

    public CardTransactionsResponse fetchCardTransactions(CardTransactionsRequest request) {
        return createRequestInSession(OpenbankConstants.Urls.CARD_TRANSACTIONS)
                .body(request)
                .post(CardTransactionsResponse.class);
    }

    public IdentityResponse getUserIdentity() {
        return createRequestInSession(Urls.IDENTITY_URL).get(IdentityResponse.class);
    }
}
