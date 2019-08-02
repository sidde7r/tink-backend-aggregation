package se.tink.backend.aggregation.agents.nxgen.de.banks.fints;

import java.util.List;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchInvestmentAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.FetchTransferDestinationsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshInvestmentAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshTransferDestinationExecutor;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.accounts.checking.FinTsAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.accounts.checking.FinTsInvestmentFetcher;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.accounts.checking.FinTsTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.accounts.checking.FinTsTransferDestinationFetcher;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.authenticator.FinTsAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.configuration.FinTsIntegrationConfiguration;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.session.FinTsSessionHandler;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.investment.InvestmentRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class FinTsAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor,
                RefreshInvestmentAccountsExecutor,
                RefreshTransferDestinationExecutor {
    private FinTsApiClient apiClient;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private final InvestmentRefreshController investmentRefreshController;
    private final TransferDestinationRefreshController transferDestinationRefreshController;

    public FinTsAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
        String[] payload = request.getProvider().getPayload().split(" ");
        FinTsConfiguration configuration =
                new se.tink.backend.aggregation.agents.nxgen.de.banks.fints.FinTsConfiguration(
                        payload[0],
                        payload[1],
                        request.getCredentials().getField(Field.Key.USERNAME),
                        request.getCredentials().getField(Field.Key.PASSWORD));

        this.apiClient = new FinTsApiClient(this.client, configuration, persistentStorage);

        this.transactionalAccountRefreshController =
                constructTransactionalAccountRefreshController();

        this.investmentRefreshController =
                new InvestmentRefreshController(
                        metricRefreshController,
                        updateController,
                        new FinTsInvestmentFetcher(apiClient));

        this.transferDestinationRefreshController =
                new TransferDestinationRefreshController(
                        metricRefreshController, new FinTsTransferDestinationFetcher(apiClient));
    }

    @Override
    public void setConfiguration(AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);

        FinTsIntegrationConfiguration finTsIntegrationConfiguration =
                configuration
                        .getIntegrations()
                        .getIntegration(
                                FinTsConstants.INTEGRATION_NAME,
                                FinTsIntegrationConfiguration.class)
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                String.format("FinTs integration not configured")));

        this.persistentStorage.put(
                FinTsConstants.Storage.REG_NUMBER, finTsIntegrationConfiguration.getRegNumber());
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new PasswordAuthenticationController(new FinTsAuthenticator(apiClient));
    }

    @Override
    public FetchAccountsResponse fetchCheckingAccounts() {
        return transactionalAccountRefreshController.fetchCheckingAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchCheckingTransactions() {
        return transactionalAccountRefreshController.fetchCheckingTransactions();
    }

    @Override
    public FetchAccountsResponse fetchSavingsAccounts() {
        return transactionalAccountRefreshController.fetchSavingsAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchSavingsTransactions() {
        return transactionalAccountRefreshController.fetchSavingsTransactions();
    }

    private TransactionalAccountRefreshController constructTransactionalAccountRefreshController() {
        FinTsTransactionFetcher transactionFetcher = new FinTsTransactionFetcher(apiClient);
        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                new FinTsAccountFetcher(apiClient),
                new TransactionFetcherController<>(
                        this.transactionPaginationHelper,
                        new TransactionDatePaginationController<>(transactionFetcher)));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new FinTsSessionHandler(apiClient);
    }

    @Override
    public FetchInvestmentAccountsResponse fetchInvestmentAccounts() {
        return investmentRefreshController.fetchInvestmentAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchInvestmentTransactions() {
        return investmentRefreshController.fetchInvestmentTransactions();
    }

    @Override
    public FetchTransferDestinationsResponse fetchTransferDestinations(List<Account> accounts) {
        return transferDestinationRefreshController.fetchTransferDestinations(accounts);
    }
}
