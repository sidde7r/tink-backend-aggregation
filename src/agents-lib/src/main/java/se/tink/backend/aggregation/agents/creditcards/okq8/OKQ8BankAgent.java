package se.tink.backend.aggregation.agents.creditcards.okq8;

import com.google.common.util.concurrent.Uninterruptibles;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource.Builder;
import com.sun.jersey.client.apache4.ApacheHttpClient4;
import java.text.ParseException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.AbstractAgent;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.DeprecatedRefreshExecutor;
import se.tink.backend.aggregation.agents.creditcards.okq8.model.LoginFailedResponse;
import se.tink.backend.aggregation.agents.creditcards.okq8.model.LoginRequest;
import se.tink.backend.aggregation.agents.creditcards.okq8.model.LoginResponse;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.rpc.Account;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.backend.aggregation.rpc.CredentialsRequest;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.agents.models.Transaction;

public class OKQ8BankAgent extends AbstractAgent implements DeprecatedRefreshExecutor {
    private final ApacheHttpClient4 client;
    private Credentials credentials;

    private static final String BASE_URL = "https://okq8-bankapp.herokuapp.com/";
    private static final String LOGIN_URL = BASE_URL + "api/login";
    private boolean hasRefreshed = false;

    /**
     * Since both auth is done and transcations are gathered in one single request we save response here for later
     */
    private Optional<LoginResponse> loginResponseFromAuthenticationRequest;


    protected static Builder createClientRequest(String uri, Client client, String aggregator) {
        return client.resource(uri)
                .header("User-Agent", aggregator)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .accept("*/*")
                .acceptLanguage("sv-se");
    }

    public OKQ8BankAgent(CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context);

        client = clientFactory.createCookieClient(context.getLogOutputStream());
        credentials = request.getCredentials();
        loginResponseFromAuthenticationRequest =  Optional.empty();
    }

    @Override
    public boolean login() throws AuthenticationException, AuthorizationException {
        loginResponseFromAuthenticationRequest = Optional.empty();

        LoginResponse loginResponse = loginAndFetchAllTransactions();

        // Sometimes we get en empty response from OKQ8.
        // Let's retry a couple of times to see if this solves anything.
        for (int i = 0; i < 3; i++) {
            if (isFetchAgain(loginResponse)) {
                // Don't fire all request at once
                Uninterruptibles.sleepUninterruptibly(2000, TimeUnit.MILLISECONDS);

                loginResponse = loginAndFetchAllTransactions();
                continue;
            }

            break;
        }

        if (isFetchAgain(loginResponse)) {
            throw new IllegalStateException("okq8 - login failed - could not fetch necessary account info");
        }

        loginResponseFromAuthenticationRequest = Optional.of(loginResponse);
        return true;
    }

    private boolean isFetchAgain(LoginResponse loginResponse) {
        return loginResponse.getAccountData() == null ||
                loginResponse.getAccountData().getAccount() == null ||
                loginResponse.getAccountData().getAccount().isEmpty();
    }

    private LoginResponse loginAndFetchAllTransactions() throws LoginException {
        LoginRequest loginRequest = createLoginRequestForCredentials(credentials);
        ClientResponse response = createClientRequest(LOGIN_URL, client, DEFAULT_USER_AGENT)
                .post(ClientResponse.class, loginRequest);

        ensureLoginSuccessful(response);

        return response.getEntity(LoginResponse.class);
    }

    private LoginRequest createLoginRequestForCredentials(Credentials credentials) {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setPassword(credentials.getPassword());

        String tenDigitUserName = credentials.getUsername().substring(2);
        loginRequest.setUsername(tenDigitUserName);

        return loginRequest;
    }

    private void ensureLoginSuccessful(ClientResponse loginResponse) throws LoginException {
        if (!Objects.equals(loginResponse.getStatus(), 200)) {
            LoginFailedResponse loginFailedResponse = loginResponse.getEntity(LoginFailedResponse.class);
            switch (loginResponse.getStatus()) {
            case 400:
                if (loginFailedResponse.getMessage() != null &&
                        loginFailedResponse.getMessage().toLowerCase().matches(".*ogiltigt? personnummer.*")) {
                    throw LoginError.INCORRECT_CREDENTIALS.exception();
                }
                // fall through
            default:
                // TODO: Implement nicer errors from LoginError to not affect our login stats
                throw new IllegalStateException(
                        String.format("#login-refactoring - OKQ8 - Login failed with status: %s Reason: %s Message: %s",
                                loginResponse.getStatus(), loginFailedResponse.getReason(), loginFailedResponse.getMessage()));
            }
        }
    }

    @Override
    public void refresh() throws Exception {
        // The refresh command will call refresh multiple times.
        // This check ensures the refresh only runs once.
        if (hasRefreshed) {
            return;
        }
        hasRefreshed = true;

        ensureLoginDoneOnThisInstance();

        Account account = getAccount();
        List<Transaction> transactions = getTransactions();

        financialDataCacher.updateTransactions(account, transactions);
    }

    private void ensureLoginDoneOnThisInstance() {
        if (!loginResponseFromAuthenticationRequest.isPresent()) {
            throw new IllegalStateException("We should already have the entity from logging in earlier.");
        }
    }

    private Account getAccount() throws ParseException {
        return loginResponseFromAuthenticationRequest.get().getAccountData().toTinkAccount();
    }

    private List<Transaction> getTransactions() {
        return loginResponseFromAuthenticationRequest.get().getTransactionsDataAsTinkTransactions();
    }

    @Override
    public void logout() throws Exception {
        // NOP
    }
}
