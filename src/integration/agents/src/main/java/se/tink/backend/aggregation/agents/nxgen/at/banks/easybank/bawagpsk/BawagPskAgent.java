package se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CREDIT_CARDS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.INVESTMENTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.LOANS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;

import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchInvestmentAccountsResponse;
import se.tink.backend.aggregation.agents.FetchLoanAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshCreditCardAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshInvestmentAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshLoanAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.configuration.signaturekeypair.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.investment.InvestmentRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.loan.LoanRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.libraries.credentials.service.CredentialsRequest;

@AgentCapabilities({CHECKING_ACCOUNTS, SAVINGS_ACCOUNTS, CREDIT_CARDS, INVESTMENTS, LOANS})
public final class BawagPskAgent extends NextGenerationAgent
        implements RefreshInvestmentAccountsExecutor,
                RefreshLoanAccountsExecutor,
                RefreshCreditCardAccountsExecutor,
                RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor {

    private final BawagPskApiClient apiClient;
    private final InvestmentRefreshController investmentRefreshController;
    private final LoanRefreshController loanRefreshController;
    private final CreditCardRefreshController creditCardRefreshController;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;

    public BawagPskAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
        configureHttpClient(client);
        this.apiClient =
                new BawagPskApiClient(
                        this.client, sessionStorage, persistentStorage, request.getProvider());

        investmentRefreshController =
                new InvestmentRefreshController(
                        metricRefreshController,
                        updateController,
                        new BawagPskInvestmentFetcher(apiClient));

        loanRefreshController =
                new LoanRefreshController(
                        metricRefreshController,
                        updateController,
                        new BawagPskLoanFetcher(apiClient),
                        new TransactionFetcherController<>(
                                transactionPaginationHelper,
                                new TransactionDatePaginationController.Builder<>(
                                                new BawagPskTransactionFetcher<LoanAccount>(
                                                        apiClient))
                                        .build()));

        creditCardRefreshController = constructCreditCardRefreshController();

        transactionalAccountRefreshController = constructTransactionalAccountRefreshController();
    }

    protected void configureHttpClient(TinkHttpClient client) {
        BawagPskUtils.trustIntermediateCertificate(client);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new PasswordAuthenticationController(new BawagPskPasswordAuthenticator(apiClient));
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
        final BawagPskTransactionFetcher<TransactionalAccount> transactionFetcher =
                new BawagPskTransactionFetcher<TransactionalAccount>(apiClient);
        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                new BawagPskTransactionalAccountFetcher(apiClient),
                new TransactionFetcherController<>(
                        this.transactionPaginationHelper,
                        // TODO Alternatively, implement a custom pagination controller
                        // which keeps fetching
                        // transactions until AccountStatementItem/Position equals 1
                        // (signifying the earliest entry)
                        new TransactionDatePaginationController.Builder<>(transactionFetcher)
                                .build()));
    }

    @Override
    public FetchAccountsResponse fetchCreditCardAccounts() {
        return creditCardRefreshController.fetchCreditCardAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchCreditCardTransactions() {
        return creditCardRefreshController.fetchCreditCardTransactions();
    }

    private CreditCardRefreshController constructCreditCardRefreshController() {
        return new CreditCardRefreshController(
                metricRefreshController,
                updateController,
                new BawagPskCreditCardFetcher(apiClient),
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionDatePaginationController.Builder<>(
                                        new BawagPskTransactionFetcher<CreditCardAccount>(
                                                apiClient))
                                .build()));
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
        return new BawagPskSessionHandler(apiClient);
    }
}
