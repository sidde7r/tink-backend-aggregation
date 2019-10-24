package se.tink.backend.aggregation.agents.nxgen.pt.banks.santander;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.FetchInvestmentAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshIdentityDataExecutor;
import se.tink.backend.aggregation.agents.RefreshInvestmentAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.santander.SantanderConstants.STORAGE;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.santander.authenticator.SantanderPasswordAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.santander.authenticator.SantanderSessionHandler;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.santander.fetcher.SantanderInvestmentAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.santander.fetcher.SantanderInvestmentTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.santander.fetcher.SantanderTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.santander.fetcher.SantanderTransactionalAccountFetcher;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.investment.InvestmentRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.identitydata.IdentityData;

public class SantanderAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor,
                RefreshIdentityDataExecutor,
                RefreshInvestmentAccountsExecutor {

    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private final InvestmentRefreshController investmentRefreshController;
    private final SantanderApiClient apiClient;
    private final SantanderPasswordAuthenticator authenticator;

    public SantanderAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);

        apiClient = new SantanderApiClient(client, sessionStorage);
        authenticator = new SantanderPasswordAuthenticator(apiClient, sessionStorage);

        this.transactionalAccountRefreshController =
                constructTransactionalAccountRefreshController();

        this.investmentRefreshController =
                new InvestmentRefreshController(
                        metricRefreshController,
                        updateController,
                        new SantanderInvestmentAccountFetcher(apiClient),
                        new SantanderInvestmentTransactionFetcher(apiClient));
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new PasswordAuthenticationController(authenticator);
    }

    private TransactionalAccountRefreshController constructTransactionalAccountRefreshController() {
        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                new SantanderTransactionalAccountFetcher(apiClient),
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionDatePaginationController<>(
                                new SantanderTransactionFetcher(apiClient))));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new SantanderSessionHandler(apiClient, sessionStorage);
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

    @Override
    public FetchIdentityDataResponse fetchIdentityData() {
        return new FetchIdentityDataResponse(
                IdentityData.builder()
                        .setFullName(sessionStorage.get(STORAGE.CUSTOMER_NAME))
                        .setDateOfBirth(null)
                        .build());
    }

    @Override
    public FetchInvestmentAccountsResponse fetchInvestmentAccounts() {
        return investmentRefreshController.fetchInvestmentAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchInvestmentTransactions() {
        return investmentRefreshController.fetchInvestmentTransactions();
    }
}
