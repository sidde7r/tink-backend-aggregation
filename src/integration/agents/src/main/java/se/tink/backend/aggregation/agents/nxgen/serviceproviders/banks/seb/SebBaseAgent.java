package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb;

import java.util.Collections;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.FetchInvestmentAccountsResponse;
import se.tink.backend.aggregation.agents.FetchLoanAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshCreditCardAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshIdentityDataExecutor;
import se.tink.backend.aggregation.agents.RefreshInvestmentAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshLoanAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.authenticator.SebBankIdAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.authenticator.SebTokenGenratorAuthenticationController;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.fetcher.creditcard.SebCreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.fetcher.investment.SebInvestmentFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.fetcher.loan.SebLoanFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.fetcher.transactionalaccount.TransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.fetcher.transactionalaccount.TransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.fetcher.transactionalaccount.UpcomingTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.session.SebSessionHandler;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.TypedAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.TypedAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.investment.InvestmentRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.loan.LoanRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

public abstract class SebBaseAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor,
                RefreshIdentityDataExecutor,
                RefreshCreditCardAccountsExecutor,
                RefreshInvestmentAccountsExecutor,
                RefreshLoanAccountsExecutor {
    protected final SebApiClient apiClient;
    protected final TransactionalAccountRefreshController transactionalAccountRefreshController;
    protected final SebSessionStorage sebSessionStorage;
    protected final CreditCardRefreshController creditCardRefreshController;
    protected final LoanRefreshController loanRefreshController;
    protected final InvestmentRefreshController investmentFetcher;
    protected final SebBaseConfiguration sebConfiguration;

    public SebBaseAgent(
            AgentComponentProvider agentComponentProvider, SebBaseConfiguration sebConfiguration) {
        super(agentComponentProvider);
        this.sebConfiguration = sebConfiguration;
        sebSessionStorage = new SebSessionStorage(sessionStorage, sebConfiguration);
        apiClient = new SebApiClient(client, sebConfiguration, sebSessionStorage);
        transactionalAccountRefreshController = constructTransactionalAccountRefreshController();
        creditCardRefreshController = constructCreditCardRefreshController();
        loanRefreshController = constructLoanRefreshController();
        investmentFetcher = constructInvestmentRefreshController();
    }

    protected TransactionalAccountRefreshController
            constructTransactionalAccountRefreshController() {
        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                new TransactionalAccountFetcher(apiClient, sebSessionStorage, sebConfiguration),
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionKeyPaginationController<>(new TransactionFetcher(apiClient)),
                        new UpcomingTransactionFetcher(apiClient)));
    }

    protected CreditCardRefreshController constructCreditCardRefreshController() {
        SebCreditCardFetcher cardFetcher = new SebCreditCardFetcher(apiClient, sebSessionStorage);
        return new CreditCardRefreshController(
                metricRefreshController, updateController, cardFetcher, cardFetcher);
    }

    private InvestmentRefreshController constructInvestmentRefreshController() {
        return new InvestmentRefreshController(
                metricRefreshController, updateController, new SebInvestmentFetcher(apiClient));
    }

    private LoanRefreshController constructLoanRefreshController() {
        return new LoanRefreshController(
                metricRefreshController, updateController, new SebLoanFetcher(apiClient));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new SebSessionHandler(apiClient, sebSessionStorage);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new TypedAuthenticationController(constructAuthenticators());
    }

    private TypedAuthenticator[] constructAuthenticators() {
        return new TypedAuthenticator[] {
            new BankIdAuthenticationController<>(
                    supplementalInformationController,
                    new SebBankIdAuthenticator(apiClient, sebConfiguration),
                    persistentStorage,
                    credentials),
            new SebTokenGenratorAuthenticationController(apiClient, supplementalInformationHelper)
        };
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
        return transactionalAccountRefreshController.fetchCheckingTransactions();
    }

    @Override
    public FetchIdentityDataResponse fetchIdentityData() {
        return new FetchIdentityDataResponse(sebSessionStorage.getIdentityData());
    }

    @Override
    public FetchAccountsResponse fetchCreditCardAccounts() {
        return creditCardRefreshController.fetchCreditCardAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchCreditCardTransactions() {
        return creditCardRefreshController.fetchCreditCardTransactions();
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
    public FetchInvestmentAccountsResponse fetchInvestmentAccounts() {
        return investmentFetcher.fetchInvestmentAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchInvestmentTransactions() {
        return new FetchTransactionsResponse(Collections.emptyMap());
    }
}
