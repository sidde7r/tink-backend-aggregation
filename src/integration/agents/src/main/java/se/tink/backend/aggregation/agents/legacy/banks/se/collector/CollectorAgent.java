package se.tink.backend.aggregation.agents.banks.se.collector;

import com.google.common.util.concurrent.Uninterruptibles;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.apache.http.HttpStatus;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsStatus;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.AbstractAgent;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.FetchTransferDestinationsResponse;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshTransferDestinationExecutor;
import se.tink.backend.aggregation.agents.TransferExecutionException;
import se.tink.backend.aggregation.agents.TransferExecutor;
import se.tink.backend.aggregation.agents.banks.se.collector.models.CollectAuthenticationResponse;
import se.tink.backend.aggregation.agents.banks.se.collector.models.ErrorResponse;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.backend.aggregation.agents.models.Transaction;
import se.tink.backend.aggregation.agents.models.TransferDestinationPattern;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.constants.CommonHeaders;
import se.tink.backend.aggregation.nxgen.http.filter.ClientFilterFactory;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.i18n.LocalizableKey;
import se.tink.libraries.net.TinkApacheHttpClient4;
import se.tink.libraries.signableoperation.enums.SignableOperationStatuses;
import se.tink.libraries.transfer.rpc.Transfer;

public class CollectorAgent extends AbstractAgent
        implements RefreshSavingsAccountsExecutor,
                RefreshTransferDestinationExecutor,
                TransferExecutor {
    private static final int MAX_ATTEMPTS = 90;

    private final CollectorApiClient apiClient;
    private final Credentials credentials;
    private final Catalog catalog;
    private final TinkApacheHttpClient4 httpClient;

    public CollectorAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context);

        credentials = request.getCredentials();
        catalog = context.getCatalog();
        httpClient = clientFactory.createCustomClient(context.getLogOutputStream());
        apiClient = new CollectorApiClient(httpClient, CommonHeaders.DEFAULT_USER_AGENT);
    }

    @Override
    public void setConfiguration(AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);
        apiClient.setSubscriptionKey(
                configuration.getAggregationWorker().getCollectorSubscriptionKey());
    }

    @Override
    public boolean login() throws AuthenticationException, AuthorizationException {
        switch (credentials.getType()) {
            case MOBILE_BANKID:
                try {
                    authenticateWithMobileBankID(credentials.getField(Field.Key.USERNAME));
                } catch (UniformInterfaceException e) {
                    ClientResponse response = e.getResponse();

                    if (response.getStatus() == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
                        throw BankIdError.ALREADY_IN_PROGRESS.exception();
                    }

                    if (response.getStatus() == HttpStatus.SC_BAD_REQUEST) {
                        ErrorResponse errorResponse = response.getEntity(ErrorResponse.class);

                        if (errorResponse.isErrorDueToRepeatedRequests()) {
                            throw BankIdError.CANCELLED.exception(
                                    new LocalizableKey(
                                            "Because of repeated attempts the authentication request has been"
                                                    + " cancelled. Please try again later."));
                        }
                    }

                    throw e;
                }
                break;
            case PASSWORD:
                return autoAuthenticate(credentials);
            default:
                throw new IllegalStateException("Authentication method not implemented");
        }

        return true;
    }

    private boolean autoAuthenticate(Credentials credentials) {
        Optional<String> accessToken = credentials.getSensitivePayload(Field.Key.ACCESS_TOKEN);
        if (accessToken.isPresent() && apiClient.isAlive(accessToken.get())) {
            return true;
        }

        try {
            log.info("Invalid access_token, request new access_token using refresh_token");
            Optional<String> refreshToken = credentials.getSensitivePayload(Field.Key.PASSWORD);
            CollectAuthenticationResponse response =
                    apiClient.refreshAccessToken(refreshToken.orElse(null));

            if (response.isValid()) {
                credentials.setSensitivePayload(Field.Key.ACCESS_TOKEN, response.getAccessToken());
                credentials.setSensitivePayload(Field.Key.PASSWORD, response.getRefreshToken());

                systemUpdater.updateCredentialsExcludingSensitiveInformation(credentials, false);
                apiClient.rememberAccessToken(response.getAccessToken());

                return true;
            }

            log.warn("Invalid response: " + response);
        } catch (IllegalArgumentException | UniformInterfaceException e) {
            log.info(String.format("Invalid refreshToken, message=%s", e.getMessage()));
        }

        // Couldn't authenticate automatically, reset credentials
        credentials.setType(CredentialsTypes.MOBILE_BANKID);
        credentials.setSensitivePayload(Field.Key.ACCESS_TOKEN, null);
        credentials.setSensitivePayload(Field.Key.PASSWORD, null);
        credentials.setStatus(CredentialsStatus.UNCHANGED);
        systemUpdater.updateCredentialsExcludingSensitiveInformation(credentials, true);

        return false;
    }

    private void authenticateWithMobileBankID(String username) throws BankIdException {
        String sessionId = apiClient.initBankId(BankIdOperationType.AUTH, username);
        openBankID();

        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            Optional<CollectAuthenticationResponse> response =
                    apiClient.collectBankId(sessionId, CollectAuthenticationResponse.class);

            if (response.isPresent()) {
                CollectAuthenticationResponse authenticationResponse = response.get();

                credentials.setType(CredentialsTypes.PASSWORD);
                credentials.setSensitivePayload(
                        Field.Key.PASSWORD, authenticationResponse.getRefreshToken());
                credentials.setSensitivePayload(
                        Field.Key.ACCESS_TOKEN, authenticationResponse.getAccessToken());

                systemUpdater.updateCredentialsExcludingSensitiveInformation(credentials, false);

                apiClient.rememberAccessToken(authenticationResponse.getAccessToken());
                return;
            }

            Uninterruptibles.sleepUninterruptibly(2, TimeUnit.SECONDS);
        }

        throw BankIdError.TIMEOUT.exception();
    }

    @Override
    public void execute(Transfer transfer) throws Exception, TransferExecutionException {
        switch (transfer.getType()) {
            case BANK_TRANSFER:
                // It's only possible to withdraw money to a predefined destination account
                makeWithdrawal(transfer);
                break;
            default:
                throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                        .setMessage(
                                String.format("execute(%s) not implemented", transfer.getType()))
                        .setEndUserMessage("Not implemented.")
                        .build();
        }

        apiClient.clearAccountsCache();
    }

    @Override
    public void update(Transfer transfer) throws Exception, TransferExecutionException {
        log.info(transfer, "Update not implemented");
    }

    private void makeWithdrawal(Transfer transfer) {
        Account sourceAccount = findAccountByIdentifier(transfer.getSource());

        if (!isSelectedWithdrawalAccount(sourceAccount, transfer)) {
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setEndUserMessage(
                            catalog.getString(
                                    TransferExecutionException.EndUserMessage.INVALID_DESTINATION))
                    .setMessage("Destination doesn't match the predefined withdrawal account")
                    .build();
        }

        apiClient.makeWithdrawal(transfer, sourceAccount);
    }

    private Account findAccountByIdentifier(AccountIdentifier identifier) {
        for (Account account : apiClient.getAccounts()) {
            if (account.definedBy(identifier)) {
                return account;
            }
        }

        throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                .setEndUserMessage(
                        catalog.getString(
                                TransferExecutionException.EndUserMessage.SOURCE_NOT_FOUND))
                .setMessage("Couldn't find source account")
                .build();
    }

    private boolean isSelectedWithdrawalAccount(Account sourceAccount, Transfer transfer) {
        AccountIdentifier withdrawalIdentifier =
                apiClient.getWithdrawalIdentifierFor(sourceAccount);

        return Objects.equals(withdrawalIdentifier, transfer.getDestination());
    }

    @Override
    public void logout() throws Exception {
        log.info("Logout not implemented");
    }

    @Override
    public void attachHttpFilters(ClientFilterFactory filterFactory) {
        filterFactory.addClientFilter(httpClient);
    }

    /////////// Refresh Executor Refactor /////////////

    @Override
    public FetchAccountsResponse fetchSavingsAccounts() {
        return new FetchAccountsResponse(apiClient.getAccounts());
    }

    @Override
    public FetchTransactionsResponse fetchSavingsTransactions() {
        Map<Account, List<Transaction>> transactionsMap = new HashMap<>();
        for (Account account : apiClient.getAccounts()) {
            List<Transaction> transactions = apiClient.fetchTransactionsFor(account);

            transactionsMap.put(account, transactions);
        }

        return new FetchTransactionsResponse(transactionsMap);
    }

    @Override
    public FetchTransferDestinationsResponse fetchTransferDestinations(List<Account> accounts) {
        Map<Account, List<TransferDestinationPattern>> destinations = new HashMap<>();
        for (Account account : accounts) {
            SwedishIdentifier withdrawalIdentifier = apiClient.getWithdrawalIdentifierFor(account);

            if (withdrawalIdentifier == null) {
                return new FetchTransferDestinationsResponse(Collections.emptyMap());
            }

            TransferDestinationPattern pattern =
                    TransferDestinationPattern.createForSingleMatch(withdrawalIdentifier);

            destinations.put(account, Collections.singletonList(pattern));
        }

        return new FetchTransferDestinationsResponse(destinations);
    }

    ///////////////////////////////////////////////////
}
