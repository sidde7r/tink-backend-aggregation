package se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi;

import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchInvestmentAccountsResponse;
import se.tink.backend.aggregation.agents.FetchLoanAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshCreditCardAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshInvestmentAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshLoanAccountsExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.authentication.BancoBpiAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.entity.BancoBpiEntityManager;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.product.account.BancoBpiTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.product.account.BancoBpiTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.product.creditcard.BancoBpiCreditCardAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.product.creditcard.BancoBpiCreditCardTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.product.investment.BancoBpiInvestmentAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.product.loan.BancoBpiLoanAccountFetcher;
import se.tink.backend.aggregation.configuration.signaturekeypair.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.SubsequentProgressiveGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.ProductionAgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.StatelessProgressiveAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.SteppableAuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.SteppableAuthenticationResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.investment.InvestmentRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.loan.LoanRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationFormer;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.credentials.service.CredentialsRequest;

@AgentCapabilities(generateFromImplementedExecutors = true)
public final class BancoBpiAgent extends SubsequentProgressiveGenerationAgent
        implements RefreshCheckingAccountsExecutor,
                RefreshCreditCardAccountsExecutor,
                RefreshInvestmentAccountsExecutor,
                RefreshLoanAccountsExecutor {

    private StatelessProgressiveAuthenticator authenticator;
    private TransactionalAccountRefreshController transactionalAccountRefreshController;
    private BancoBpiEntityManager entityManager;
    private CreditCardRefreshController creditCardRefreshController;
    private LoanRefreshController loanRefreshController;
    private InvestmentRefreshController investmentRefreshController;
    private BancoBpiClientApi bancoBpiApi;

    public BancoBpiAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(ProductionAgentComponentProvider.create(request, context, signatureKeyPair));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }

    @Override
    public StatelessProgressiveAuthenticator getAuthenticator() {
        if (authenticator == null) {
            authenticator =
                    new BancoBpiAuthenticator(
                            client,
                            new SupplementalInformationFormer(request.getProvider()),
                            getEntityManager());
        }
        return authenticator;
    }

    @Override
    public FetchAccountsResponse fetchCheckingAccounts() {
        return getTransactionalAccountRefreshController().fetchCheckingAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchCheckingTransactions() {
        return getTransactionalAccountRefreshController().fetchCheckingTransactions();
    }

    @Override
    public SteppableAuthenticationResponse login(SteppableAuthenticationRequest request)
            throws Exception {
        SteppableAuthenticationResponse response = super.login(request);
        getEntityManager().saveEntities();
        return response;
    }

    @Override
    public FetchAccountsResponse fetchCreditCardAccounts() {
        return getCreditCardRefreshController().fetchCreditCardAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchCreditCardTransactions() {
        return getCreditCardRefreshController().fetchCreditCardTransactions();
    }

    @Override
    public FetchInvestmentAccountsResponse fetchInvestmentAccounts() {
        return getInvestmentRefreshController().fetchInvestmentAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchInvestmentTransactions() {
        return getInvestmentRefreshController().fetchInvestmentTransactions();
    }

    @Override
    public FetchLoanAccountsResponse fetchLoanAccounts() {
        return getLoanRefreshController().fetchLoanAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchLoanTransactions() {
        return getLoanRefreshController().fetchLoanTransactions();
    }

    private BancoBpiEntityManager getEntityManager() {
        if (entityManager == null) {
            entityManager = new BancoBpiEntityManager(persistentStorage, sessionStorage);
        }
        return entityManager;
    }

    private TransactionalAccountRefreshController getTransactionalAccountRefreshController() {

        if (transactionalAccountRefreshController == null) {
            transactionalAccountRefreshController =
                    new TransactionalAccountRefreshController(
                            metricRefreshController,
                            updateController,
                            new BancoBpiTransactionalAccountFetcher(
                                    getBancoBpiClientApi(), getEntityManager()),
                            createTransactionFetcher());
        }
        return transactionalAccountRefreshController;
    }

    private TransactionFetcherController<TransactionalAccount> createTransactionFetcher() {
        BancoBpiTransactionFetcher bancoBpiTransactionFetcher =
                new BancoBpiTransactionFetcher(getBancoBpiClientApi());
        TransactionPagePaginationController<TransactionalAccount>
                transactionPagePaginationController =
                        new TransactionPagePaginationController<>(bancoBpiTransactionFetcher, 1);
        return new TransactionFetcherController<>(
                transactionPaginationHelper, transactionPagePaginationController);
    }

    private CreditCardRefreshController getCreditCardRefreshController() {
        if (creditCardRefreshController == null) {
            creditCardRefreshController =
                    new CreditCardRefreshController(
                            metricRefreshController,
                            updateController,
                            new BancoBpiCreditCardAccountFetcher(getBancoBpiClientApi()),
                            new TransactionFetcherController<>(
                                    transactionPaginationHelper,
                                    new TransactionPagePaginationController<>(
                                            new BancoBpiCreditCardTransactionFetcher(
                                                    getBancoBpiClientApi()),
                                            1)));
        }
        return creditCardRefreshController;
    }

    private LoanRefreshController getLoanRefreshController() {
        if (loanRefreshController == null) {
            loanRefreshController =
                    new LoanRefreshController(
                            metricRefreshController,
                            updateController,
                            new BancoBpiLoanAccountFetcher(getBancoBpiClientApi()));
        }
        return loanRefreshController;
    }

    private InvestmentRefreshController getInvestmentRefreshController() {
        if (investmentRefreshController == null) {
            investmentRefreshController =
                    new InvestmentRefreshController(
                            metricRefreshController,
                            updateController,
                            new BancoBpiInvestmentAccountFetcher(getBancoBpiClientApi()));
        }
        return investmentRefreshController;
    }

    private BancoBpiClientApi getBancoBpiClientApi() {
        if (bancoBpiApi == null) {
            bancoBpiApi = new BancoBpiClientApi(client, getEntityManager());
        }
        return bancoBpiApi;
    }
}
