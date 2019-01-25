package se.tink.backend.aggregation.agents.creditcards.coop.v2;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import java.util.List;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import se.tink.backend.aggregation.agents.creditcards.coop.v2.model.AccountEntity;
import se.tink.backend.aggregation.agents.creditcards.coop.v2.model.AuthenticateResult;
import se.tink.backend.aggregation.agents.creditcards.coop.v2.model.BaseRequest;
import se.tink.backend.aggregation.agents.creditcards.coop.v2.model.LoginRequest;
import se.tink.backend.aggregation.agents.creditcards.coop.v2.model.LoginResponse;
import se.tink.backend.aggregation.agents.creditcards.coop.v2.model.SummaryResponse;
import se.tink.backend.aggregation.agents.creditcards.coop.v2.model.TransactionRequest;
import se.tink.backend.aggregation.agents.creditcards.coop.v2.model.TransactionResponse;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.metrics.MetricRegistry;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.backend.aggregation.rpc.Field;
import se.tink.backend.system.rpc.Transaction;

public class CoopApiClient {

    private static final MetricId METRIC_COOP_GET_TRANSACTIONS = MetricId.newId("coop_get_transactions");
    private static final String APPLICATION_ID = "687D17CB-85C3-4547-9F8D-A346C7008EB1";
    private static final String BASE_URL = "https://www.coop.se/ExternalServices/V4";
    private static final String LOGIN_URL = BASE_URL + "/UserServiceV4.svc/Authenticate";
    private static final String TRANSACTION_URL = BASE_URL + "/FinancialServiceV4.svc/GetTransactions";
    private static final String SUMMARY_URL = BASE_URL + "/UserServiceV4.svc/GetUserSummary";
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final AggregationLogger log = new AggregationLogger(CoopApiClient.class);
    private final Client client;
    private final Credentials credentials;
    private final MetricRegistry registry;
    private String token;
    private String userId;

    public CoopApiClient(Client client, Credentials credentials, MetricRegistry registry) {
        this.client = client;
        this.credentials = credentials;
        this.registry = registry;
    }

    private WebResource.Builder createClientRequest(String url) {
        return client.resource(url)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON);
    }

    private <T> T post(String url, Class<T> responseType, Object requestEntity) {
        return createClientRequest(url).post(responseType, requestEntity);
    }

    public boolean loginWithPassword() throws LoginException {
        LoginRequest loginRequest = buildLoginRequest();

        try {
            LoginResponse loginResponse = post(LOGIN_URL, LoginResponse.class, loginRequest);
            AuthenticateResult authenticateResult = loginResponse.getAuthenticateResult();

            Preconditions.checkNotNull(authenticateResult);

            this.token = authenticateResult.getToken();
            this.userId = String.valueOf(authenticateResult.getUserId());

            return true;
        } catch (UniformInterfaceException e) {
            if (e.getResponse().getStatus() == Response.Status.UNAUTHORIZED.getStatusCode() &&
                    Boolean.valueOf(e.getResponse().getHeaders().getFirst("jsonError"))) {
                throw LoginError.INCORRECT_CREDENTIALS.exception();
            }

            throw e;
        }
    }

    public List<AccountEntity> getAccounts() {
        return fetchAccounts().getAccounts();
    }

    private SummaryResponse fetchAccounts() {
        BaseRequest baseRequest = buildSummaryRequest();
        return post(SUMMARY_URL, SummaryResponse.class, baseRequest);
    }

    public List<Transaction> getTransactions(int accountType, int pageSize) {
        // We've seen a lot of 500 responses on first try to get transactions. When retrying it works.
        // Let's retry in code and see if that solves it.
        try {
            TransactionResponse transactionResponse = fetchTransactions(accountType, pageSize);
            registry.meter(METRIC_COOP_GET_TRANSACTIONS.label("outcome", "success")).inc();

            return transactionResponse.toTransactions();
        } catch (UniformInterfaceException firstExc) {
            if (firstExc.getResponse().getStatus() != 500) {
                throw firstExc;
            }

            log.warn("Failed fetching transactions. Retrying...", firstExc);
            return retryGetTransactions(accountType, pageSize);
        }
    }

    private List<Transaction> retryGetTransactions(int accountType, int pageSize) {
        try {
            TransactionResponse transactionResponse = fetchTransactions(accountType, pageSize);
            registry.meter(METRIC_COOP_GET_TRANSACTIONS.label("outcome", "retry_ok")).inc();

            return transactionResponse.toTransactions();
        } catch (UniformInterfaceException retryExc) {
            log.warn("Failed retry fetching transactions.", retryExc);
            registry.meter(METRIC_COOP_GET_TRANSACTIONS.label("outcome", "retry_failure")).inc();

            throw retryExc;
        }
    }

    private TransactionResponse fetchTransactions(int accountType, int pageSize) {
        TransactionRequest transactionRequest = buildTransactionRequest(accountType, pageSize);
        return post(TRANSACTION_URL, TransactionResponse.class, transactionRequest);
    }

    private LoginRequest buildLoginRequest() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setApplicationId(APPLICATION_ID);
        loginRequest.setUsername(credentials.getField(Field.Key.USERNAME));
        loginRequest.setPassword(credentials.getField(Field.Key.PASSWORD));

        return loginRequest;
    }

    private BaseRequest buildSummaryRequest() {
        BaseRequest baseRequest = new BaseRequest();
        baseRequest.setApplicationId(APPLICATION_ID);
        baseRequest.setToken(token);
        baseRequest.setUserId(userId);

        return baseRequest;
    }

    private TransactionRequest buildTransactionRequest(int accountType, int pageSize) {
        TransactionRequest transactionRequest = new TransactionRequest();
        transactionRequest.setMaxNrOfTransactions(pageSize);
        transactionRequest.setApplicationId(APPLICATION_ID);
        transactionRequest.setAccountType(accountType);
        transactionRequest.setToken(token);
        transactionRequest.setFromYear(2000); // Something that worked when testing this
        transactionRequest.setUserId(userId);

        return transactionRequest;
    }

}
