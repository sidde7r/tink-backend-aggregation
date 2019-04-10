package se.tink.backend.aggregation.agents.nxgen.se.creditcards.coop;

import com.google.common.base.Strings;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.core.HttpHeaders;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.se.creditcards.coop.authenticator.rpc.AuthenticateRequest;
import se.tink.backend.aggregation.agents.nxgen.se.creditcards.coop.authenticator.rpc.AuthenticateResponse;
import se.tink.backend.aggregation.agents.nxgen.se.creditcards.coop.fetcher.rpc.TransactionsRequest;
import se.tink.backend.aggregation.agents.nxgen.se.creditcards.coop.fetcher.rpc.TransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.creditcards.coop.rpc.UserSummaryResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class CoopApiClient {

    private final TinkHttpClient client;
    private final SessionStorage sessionStorage;

    public CoopApiClient(TinkHttpClient client, SessionStorage sessionStorage) {
        this.client = client;
        this.sessionStorage = sessionStorage;
    }

    public AuthenticateResponse authenticate(String username, String password)
            throws AuthenticationException {
        AuthenticateRequest authenticateRequest = new AuthenticateRequest(username, password);

        try {

            return createRequest(CoopConstants.Url.AUTHENTICATE)
                    .post(AuthenticateResponse.class, authenticateRequest);
        } catch (HttpResponseException e) {

            int status = e.getResponse().getStatus();
            if (status == 401) {

                throw LoginError.INCORRECT_CREDENTIALS.exception();
            }

            throw e;
        }
    }

    public UserSummaryResponse getUserSummary() {
        return createRequest(CoopConstants.Url.USER_SUMMARY).get(UserSummaryResponse.class);
    }

    // fetch transactions
    // this is done in three tries where we fetch 200 first time, 1000 second time and 10000 last
    // time
    // this is because Coop API only returns a number of transactions, there is no offsetting
    // we fetch e few the first time since most accounts are fetched every day
    // We will not fetch more than 10000 transactions
    public PaginatorResponse fetchTransactions(int page, int accountType) {
        if (page > 2) {
            return PaginatorResponseImpl.createEmpty(false);
        }

        LocalDate now = LocalDate.now();
        // offset is already fetched transactions
        int offset = 0;
        if (page > 0) {
            offset = CoopConstants.Account.TRANSACTION_BATCH_SIZE.get(page - 1);
        }

        int maxNoTransactions = CoopConstants.Account.TRANSACTION_BATCH_SIZE.get(page);
        int fromYear = CoopConstants.Account.YEAR_TO_START_FETCH;

        TransactionsRequest transactionsRequest =
                TransactionsRequest.create(maxNoTransactions, accountType, fromYear);

        return createRequest(CoopConstants.Url.TRANSACTIONS)
                .post(TransactionsResponse.class, transactionsRequest)
                .getTinkTransactions(offset, maxNoTransactions);
    }

    private RequestBuilder createRequest(URL url) {
        return client.request(url).headers(getHeaders());
    }

    private Map<String, Object> getHeaders() {
        Map<String, Object> headers = new HashMap<>(CoopConstants.Header.DEFAULT_HEADERS);

        String authHeader = getAuthHeader();
        if (!Strings.isNullOrEmpty(authHeader)) {
            headers.put(HttpHeaders.AUTHORIZATION, authHeader);
        }

        return headers;
    }

    private String getAuthHeader() {
        String token = sessionStorage.get(CoopConstants.Storage.TOKEN);
        String userId = sessionStorage.get(CoopConstants.Storage.USER_ID);

        if (Strings.isNullOrEmpty(token) || Strings.isNullOrEmpty(userId)) {
            return null;
        }

        return String.format("%s %s:%s", CoopConstants.Header.TOKEN_TYPE, userId, token);
    }
}
