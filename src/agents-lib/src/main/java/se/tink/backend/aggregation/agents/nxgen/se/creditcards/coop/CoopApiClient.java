package se.tink.backend.aggregation.agents.nxgen.se.creditcards.coop;

import com.google.common.base.Strings;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.core.HttpHeaders;
import se.tink.backend.aggregation.agents.nxgen.se.creditcards.coop.authenticator.rpc.AuthenticateRequest;
import se.tink.backend.aggregation.agents.nxgen.se.creditcards.coop.authenticator.rpc.AuthenticateResponse;
import se.tink.backend.aggregation.agents.nxgen.se.creditcards.coop.fetcher.rpc.TransactionsRequest;
import se.tink.backend.aggregation.agents.nxgen.se.creditcards.coop.fetcher.rpc.TransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.creditcards.coop.rpc.UserSummaryResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class CoopApiClient {

    private final TinkHttpClient client;
    private final SessionStorage sessionStorage;

    public CoopApiClient(TinkHttpClient client, SessionStorage sessionStorage) {
        this.client = client;
        this.sessionStorage = sessionStorage;
    }

    public AuthenticateResponse authenticate(String username, String password) {
        AuthenticateRequest authenticateRequest = new AuthenticateRequest(username, password);

        return createRequest(CoopConstants.Url.AUTHENTICATE)
                .post(AuthenticateResponse.class, authenticateRequest);
    }


    public UserSummaryResponse getUserSummary() {
        return createRequest(CoopConstants.Url.USER_SUMMARY)
                .get(UserSummaryResponse.class);
    }


    public PaginatorResponse fetchTransactions(int page, int accountType) {
        LocalDate now = LocalDate.now();
        int offset = page * CoopConstants.Account.TRANSACTION_BATCH;
        int maxNoTransactions = (page + 1) * CoopConstants.Account.TRANSACTION_BATCH;
        int fromYear = now.getYear() - CoopConstants.Account.MAX_YEAR_TO_FETCH;

        TransactionsRequest transactionsRequest = TransactionsRequest.create(maxNoTransactions, accountType, fromYear);

        return createRequest(CoopConstants.Url.TRANSACTIONS)
                .post(TransactionsResponse.class, transactionsRequest)
                .getTinkTransactions(offset, CoopConstants.Account.TRANSACTION_BATCH);
    }

    private RequestBuilder createRequest(URL url) {
        return client.request(url)
                .headers(getHeaders());
    }

    private Map<String, Object> getHeaders() {
        Map<String, Object> headers= new HashMap<>(CoopConstants.Header.DEFAULT_HEADERS);

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
