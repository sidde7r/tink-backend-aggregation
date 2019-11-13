package se.tink.backend.aggregation.agents.nxgen.pt.banks.montepio;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchInvestmentAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshCreditCardAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshInvestmentAccountsExecutor;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.montepio.authenticator.MontepioAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.montepio.fetcher.accounts.MontepioTransactionalAccountsFetcher;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.montepio.fetcher.creditcard.MontepioCreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.montepio.fetcher.investments.MontepioInvestmentAccountsFetcher;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.investment.InvestmentRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class MontepioAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor,
                RefreshInvestmentAccountsExecutor,
                RefreshCreditCardAccountsExecutor {

    private final MontepioApiClient apiClient;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private final InvestmentRefreshController investmentRefreshController;
    private final CreditCardRefreshController creditCardRefreshController;

    public MontepioAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
        this.apiClient = new MontepioApiClient(client, sessionStorage);
        this.transactionalAccountRefreshController = constructAccountRefreshController();
        this.investmentRefreshController = constructInvestmentRefreshController();
        this.creditCardRefreshController = constructCreditCardRefreshController();
    }

    @Override
    protected Authenticator constructAuthenticator() {
        MontepioAuthenticator montepioAuthenticator = new MontepioAuthenticator(apiClient);
        return new PasswordAuthenticationController(montepioAuthenticator);
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }

    private TransactionalAccountRefreshController constructAccountRefreshController() {
        MontepioTransactionalAccountsFetcher transactionalAccountsFetcher =
                new MontepioTransactionalAccountsFetcher(apiClient);
        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                transactionalAccountsFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionPagePaginationController<>(
                                transactionalAccountsFetcher, 1)));
    }

    private InvestmentRefreshController constructInvestmentRefreshController() {
        MontepioInvestmentAccountsFetcher fetcher =
                new MontepioInvestmentAccountsFetcher(apiClient);
        return new InvestmentRefreshController(
                metricRefreshController,
                updateController,
                fetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionPagePaginationController<>(fetcher, 1)));
    }

    private CreditCardRefreshController constructCreditCardRefreshController() {
        MontepioCreditCardFetcher fetcher = new MontepioCreditCardFetcher(apiClient);
        return new CreditCardRefreshController(
                metricRefreshController,
                updateController,
                fetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionPagePaginationController<>(fetcher, 1)));
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
    public FetchInvestmentAccountsResponse fetchInvestmentAccounts() {
        return investmentRefreshController.fetchInvestmentAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchInvestmentTransactions() {
        return investmentRefreshController.fetchInvestmentTransactions();
    }

    @Override
    public FetchAccountsResponse fetchCreditCardAccounts() {
        return creditCardRefreshController.fetchCreditCardAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchCreditCardTransactions() {
        return creditCardRefreshController.fetchCreditCardTransactions();
    }
}
