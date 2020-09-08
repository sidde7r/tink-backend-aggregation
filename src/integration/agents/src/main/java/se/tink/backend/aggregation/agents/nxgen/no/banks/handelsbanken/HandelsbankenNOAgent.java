package se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken;

import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchInvestmentAccountsResponse;
import se.tink.backend.aggregation.agents.FetchLoanAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshInvestmentAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshLoanAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.authenticator.HandelsbankenNOAutoAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.authenticator.HandelsbankenNOMultiFactorAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.fetcher.investment.HandelsbankenNOInvestmentFetcher;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.fetcher.loan.HandelsbankenNOLoanAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.fetcher.transactionalaccount.HandelsbankenNOAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.fetcher.transactionalaccount.HandelsbankenNOTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.filters.HandelsbankenNORetryFilter;
import se.tink.backend.aggregation.agents.utils.authentication.encap3.EncapClient;
import se.tink.backend.aggregation.configuration.signaturekeypair.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.bankid.BankIdAuthenticationControllerNO;
import se.tink.backend.aggregation.nxgen.controllers.refresh.investment.InvestmentRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.loan.LoanRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.index.TransactionIndexPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filters.retry.TimeoutRetryFilter;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class HandelsbankenNOAgent extends NextGenerationAgent
        implements RefreshInvestmentAccountsExecutor,
                RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor,
                RefreshLoanAccountsExecutor {

    private final HandelsbankenNOApiClient apiClient;
    private EncapClient encapClient;
    private final InvestmentRefreshController investmentRefreshController;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private final LoanRefreshController loanRefreshController;

    public HandelsbankenNOAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
        configureHttpClient(client);
        apiClient = new HandelsbankenNOApiClient(client, sessionStorage);
        encapClient =
                new EncapClient(
                        context,
                        request,
                        signatureKeyPair,
                        persistentStorage,
                        new HandelsbankenNOEncapConfiguration(),
                        HandelsbankenNOConstants.DEVICE_PROFILE);

        investmentRefreshController =
                new InvestmentRefreshController(
                        metricRefreshController,
                        updateController,
                        new HandelsbankenNOInvestmentFetcher(
                                apiClient, credentials.getField(Field.Key.USERNAME)));

        loanRefreshController =
                new LoanRefreshController(
                        metricRefreshController,
                        updateController,
                        new HandelsbankenNOLoanAccountFetcher(apiClient));

        transactionalAccountRefreshController = constructTransactionalAccountRefreshController();
    }

    private void configureHttpClient(TinkHttpClient client) {
        client.addFilter(
                new TimeoutRetryFilter(
                        HandelsbankenNOConstants.TimeoutFilter.NUM_TIMEOUT_RETRIES,
                        HandelsbankenNOConstants.TimeoutFilter.TIMEOUT_RETRY_SLEEP_MILLISECONDS));
        client.addFilter(
                new HandelsbankenNORetryFilter(
                        HandelsbankenNOConstants.RetryFilter.NUM_TIMEOUT_RETRIES,
                        HandelsbankenNOConstants.RetryFilter.RETRY_SLEEP_MILLISECONDS));
    }

    @Override
    protected Authenticator constructAuthenticator() {
        HandelsbankenNOMultiFactorAuthenticator multiFactorAuthenticator =
                new HandelsbankenNOMultiFactorAuthenticator(
                        apiClient,
                        sessionStorage,
                        supplementalInformationController,
                        catalog,
                        encapClient);

        HandelsbankenNOAutoAuthenticator autoAuthenticator =
                new HandelsbankenNOAutoAuthenticator(apiClient, encapClient, sessionStorage);

        return new AutoAuthenticationController(
                request,
                systemUpdater,
                new BankIdAuthenticationControllerNO(
                        supplementalRequester, multiFactorAuthenticator),
                autoAuthenticator);
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
        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                new HandelsbankenNOAccountFetcher(apiClient),
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionIndexPaginationController<>(
                                new HandelsbankenNOTransactionFetcher(apiClient))));
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
    public FetchLoanAccountsResponse fetchLoanAccounts() {
        return loanRefreshController.fetchLoanAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchLoanTransactions() {
        return loanRefreshController.fetchLoanTransactions();
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new HandelsbankenNOSessionHandler(apiClient);
    }

    void populateSessionStorage(String key, String value) {
        this.sessionStorage.put(key, value);
    }
}
