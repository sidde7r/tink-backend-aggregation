package se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchInvestmentAccountsResponse;
import se.tink.backend.aggregation.agents.FetchLoanAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshInvestmentAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshLoanAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.authenticator.NovoBancoAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.authenticator.NovoBancoSessionHandler;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.NovoBancoInvestmentAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.NovoBancoLoanAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.NovoBancoTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.NovoBancoTransactionalAccountFetcher;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.investment.InvestmentRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.loan.LoanRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class NovoBancoAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor,
                RefreshLoanAccountsExecutor,
                RefreshInvestmentAccountsExecutor {

    private final NovoBancoApiClient apiClient;
    private final NovoBancoAuthenticator authenticator;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private final LoanRefreshController loanRefreshController;
    private final InvestmentRefreshController investmentRefreshController;

    public NovoBancoAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair, true);
        apiClient = new NovoBancoApiClient(client, sessionStorage);
        authenticator = new NovoBancoAuthenticator(apiClient, sessionStorage);
        transactionalAccountRefreshController = constructTransactionalAccountRefreshController();
        loanRefreshController = constructLoanRefreshController();
        investmentRefreshController = constructInvestmentRefreshController();
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
    public FetchLoanAccountsResponse fetchLoanAccounts() {
        return loanRefreshController.fetchLoanAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchLoanTransactions() {
        return loanRefreshController.fetchLoanTransactions();
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
    public FetchInvestmentAccountsResponse fetchInvestmentAccounts() {
        return investmentRefreshController.fetchInvestmentAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchInvestmentTransactions() {
        return investmentRefreshController.fetchInvestmentTransactions();
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new PasswordAuthenticationController(authenticator);
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new NovoBancoSessionHandler(apiClient, sessionStorage);
    }

    private TransactionalAccountRefreshController constructTransactionalAccountRefreshController() {
        NovoBancoTransactionalAccountFetcher accountFetcher =
                new NovoBancoTransactionalAccountFetcher(apiClient);
        NovoBancoTransactionFetcher transactionFetcher = new NovoBancoTransactionFetcher(apiClient);

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                accountFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionPagePaginationController<>(transactionFetcher, 1)));
    }

    private LoanRefreshController constructLoanRefreshController() {
        NovoBancoLoanAccountFetcher accountFetcher = new NovoBancoLoanAccountFetcher(apiClient);
        return new LoanRefreshController(metricRefreshController, updateController, accountFetcher);
    }

    private InvestmentRefreshController constructInvestmentRefreshController() {
        return new InvestmentRefreshController(
                metricRefreshController,
                updateController,
                new NovoBancoInvestmentAccountFetcher(apiClient));
        // no transaction fetcher as mobile app does not support getting investment transactions
    }
}
