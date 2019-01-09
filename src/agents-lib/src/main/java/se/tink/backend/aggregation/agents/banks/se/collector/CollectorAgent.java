package se.tink.backend.aggregation.agents.banks.se.collector;

import com.google.common.util.concurrent.Uninterruptibles;
import com.sun.jersey.api.client.UniformInterfaceException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import se.tink.backend.aggregation.agents.AbstractAgent;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.RefreshableItemExecutor;
import se.tink.backend.aggregation.agents.TransferDestinationsResponse;
import se.tink.backend.aggregation.agents.TransferExecutionException;
import se.tink.backend.aggregation.agents.TransferExecutor;
import se.tink.backend.aggregation.agents.banks.se.collector.models.CollectAuthenticationResponse;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.backend.aggregation.nxgen.http.filter.ClientFilterFactory;
import se.tink.backend.aggregation.rpc.Account;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.backend.aggregation.rpc.CredentialsRequest;
import se.tink.backend.aggregation.rpc.CredentialsStatus;
import se.tink.backend.aggregation.rpc.CredentialsTypes;
import se.tink.backend.aggregation.rpc.Field;
import se.tink.backend.aggregation.rpc.RefreshableItem;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.core.account.TransferDestinationPattern;
import se.tink.backend.core.transfer.SignableOperationStatuses;
import se.tink.backend.core.transfer.Transfer;
import se.tink.backend.system.rpc.Transaction;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.net.TinkApacheHttpClient4;

public class CollectorAgent extends AbstractAgent implements RefreshableItemExecutor,
        TransferExecutor {
    private static final int MAX_ATTEMPTS = 90;

    private final CollectorApiClient apiClient;
    private final Credentials credentials;
    private final Catalog catalog;
    private final TinkApacheHttpClient4 httpClient;

    public CollectorAgent(CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context);

        credentials = request.getCredentials();
        catalog = context.getCatalog();
        httpClient = clientFactory.createCustomClient(context.getLogOutputStream());
        apiClient = new CollectorApiClient(httpClient, DEFAULT_USER_AGENT);
    }

    @Override
    public void setConfiguration(AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);
        apiClient.setSubscriptionKey(configuration.getAggregationWorker().getCollectorSubscriptionKey());
    }

    @Override
    public boolean login() throws AuthenticationException, AuthorizationException {
            switch (credentials.getType()) {
            case MOBILE_BANKID:
                try {
                    authenticateWithMobileBankID(credentials.getField(Field.Key.USERNAME));
                } catch (UniformInterfaceException e) {
                    if (!Objects.equals(e.getResponse().getStatus(), 500)) {
                        throw e;
                    }

                    log.info("User probably had an ongoing authentication request, try again");
                    authenticateWithMobileBankID(credentials.getField(Field.Key.USERNAME));
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
            CollectAuthenticationResponse response = apiClient.refreshAccessToken(refreshToken.orElse(null));

            if (response.isValid()) {
                credentials.setSensitivePayload(Field.Key.ACCESS_TOKEN, response.getAccessToken());
                credentials.setSensitivePayload(Field.Key.PASSWORD, response.getRefreshToken());

                context.updateCredentialsExcludingSensitiveInformation(credentials, false);
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
        context.updateCredentialsExcludingSensitiveInformation(credentials, true);

        return false;
    }

    private void authenticateWithMobileBankID(String username) throws BankIdException {
        String sessionId = apiClient.initBankId(BankIdOperationType.AUTH, username);
        openBankID();

        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            Optional<CollectAuthenticationResponse> response = apiClient.collectBankId(sessionId,
                    CollectAuthenticationResponse.class);

            if (response.isPresent()) {
                CollectAuthenticationResponse authenticationResponse = response.get();

                credentials.setType(CredentialsTypes.PASSWORD);
                credentials.setSensitivePayload(Field.Key.PASSWORD, authenticationResponse.getRefreshToken());
                credentials.setSensitivePayload(Field.Key.ACCESS_TOKEN, authenticationResponse.getAccessToken());

                context.updateCredentialsExcludingSensitiveInformation(credentials, false);

                apiClient.rememberAccessToken(authenticationResponse.getAccessToken());
                return;
            }

            Uninterruptibles.sleepUninterruptibly(2, TimeUnit.SECONDS);
        }

        throw BankIdError.TIMEOUT.exception();
    }

    @Override
    public void refresh(RefreshableItem item) {
        switch (item) {
        case SAVINGS_ACCOUNTS:
            List<Account> accounts = apiClient.getAccounts();
            context.cacheAccounts(accounts);
            break;

        case SAVINGS_TRANSACTIONS:
            for (Account account : apiClient.getAccounts()) {
                List<Transaction> transactions = apiClient.fetchTransactionsFor(account);

                context.updateTransactions(account, transactions);
            }
            break;

        case TRANSFER_DESTINATIONS:
            TransferDestinationsResponse response = new TransferDestinationsResponse();
            for (Account account : context.getUpdatedAccounts()) {
                SwedishIdentifier withdrawalIdentifier = apiClient.getWithdrawalIdentifierFor(account);

                if (withdrawalIdentifier == null) {
                    return;
                }

                TransferDestinationPattern pattern = TransferDestinationPattern.createForSingleMatch(withdrawalIdentifier);
                response.addDestination(account, pattern);
            }

            context.updateTransferDestinationPatterns(response.getDestinations());
            break;
        }
    }

    @Override
    public void execute(Transfer transfer)
            throws Exception, TransferExecutionException {
        switch (transfer.getType()) {
            case BANK_TRANSFER:
                // It's only possible to withdraw money to a predefined destination account
                makeWithdrawal(transfer);
                break;
            default:
                throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                        .setMessage(String.format("execute(%s) not implemented", transfer.getType()))
                        .setEndUserMessage("Not implemented.")
                        .build();
        }

        apiClient.clearAccountsCache();
    }

    @Override
    public void update(Transfer transfer)
            throws Exception, TransferExecutionException {
        log.info(transfer, "Update not implemented");
    }

    private void makeWithdrawal(Transfer transfer) {
        Account sourceAccount = findAccountByIdentifier(transfer.getSource());

        if (!isSelectedWithdrawalAccount(sourceAccount, transfer)) {
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setEndUserMessage(catalog.getString(TransferExecutionException.EndUserMessage.INVALID_DESTINATION))
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
                .setEndUserMessage(catalog.getString(TransferExecutionException.EndUserMessage.SOURCE_NOT_FOUND))
                .setMessage("Couldn't find source account")
                .build();
    }

    private boolean isSelectedWithdrawalAccount(Account sourceAccount, Transfer transfer) {
        AccountIdentifier withdrawalIdentifier = apiClient.getWithdrawalIdentifierFor(sourceAccount);

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
}
