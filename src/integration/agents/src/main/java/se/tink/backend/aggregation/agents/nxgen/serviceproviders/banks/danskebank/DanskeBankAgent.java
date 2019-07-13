package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank;

import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchInvestmentAccountsResponse;
import se.tink.backend.aggregation.agents.FetchLoanAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshCreditCardAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshInvestmentAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshLoanAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.DanskeBankAccountLoanFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.DanskeBankCreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.DanskeBankMultiTransactionsFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.DanskeBankTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.investment.DanskeBankInvestmentFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.filters.DanskeBankHttpFilter;
import se.tink.backend.aggregation.agents.utils.crypto.Hash;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.investment.InvestmentRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.loan.LoanRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.libraries.credentials.service.CredentialsRequest;

public abstract class DanskeBankAgent<MarketSpecificApiClient extends DanskeBankApiClient>
        extends NextGenerationAgent
        implements RefreshInvestmentAccountsExecutor,
                RefreshLoanAccountsExecutor,
                RefreshCreditCardAccountsExecutor,
                RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor {
    protected final MarketSpecificApiClient apiClient;
    protected final DanskeBankConfiguration configuration;
    protected final String deviceId;

    private final InvestmentRefreshController investmentRefreshController;
    private final LoanRefreshController loanRefreshController;
    private final CreditCardRefreshController creditCardRefreshController;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;

    public DanskeBankAgent(
            CredentialsRequest request,
            AgentContext context,
            SignatureKeyPair signatureKeyPair,
            DanskeBankConfiguration configuration) {
        super(request, context, signatureKeyPair);
        this.apiClient = createApiClient(this.client, configuration);
        this.configuration = configuration;
        this.deviceId = Hash.sha1AsHex(this.credentials.getField(Field.Key.USERNAME) + "-TINK");

        this.investmentRefreshController =
                new InvestmentRefreshController(
                        this.metricRefreshController,
                        this.updateController,
                        new DanskeBankInvestmentFetcher(this.apiClient));

        // Fetches loans from the accounts endpoint
        this.loanRefreshController =
                new LoanRefreshController(
                        this.metricRefreshController,
                        this.updateController,
                        new DanskeBankAccountLoanFetcher(
                                this.credentials, this.apiClient, this.configuration),
                        createTransactionFetcherController());

        this.creditCardRefreshController = constructCreditCardRefreshController();

        this.transactionalAccountRefreshController =
                constructTransactionalAccountRefreshController();

        // Must add the filter here because `configureHttpClient` is called before the agent
        // constructor
        // (from NextGenerationAgent constructor).
        client.addFilter(new DanskeBankHttpFilter(configuration));
    }

    protected abstract MarketSpecificApiClient createApiClient(
            TinkHttpClient client, DanskeBankConfiguration configuration);

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
                this.metricRefreshController,
                this.updateController,
                new DanskeBankTransactionalAccountFetcher(
                        this.credentials, this.apiClient, this.configuration),
                createTransactionFetcherController());
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
                this.metricRefreshController,
                this.updateController,
                new DanskeBankCreditCardFetcher(
                        this.apiClient, this.configuration.getLanguageCode()),
                createTransactionFetcherController());
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

    private <A extends Account>
            TransactionFetcherController<A> createTransactionFetcherController() {
        DanskeBankMultiTransactionsFetcher<A> transactionFetcher =
                new DanskeBankMultiTransactionsFetcher<>(
                        this.apiClient, this.configuration.getLanguageCode());
        return new TransactionFetcherController<>(
                this.transactionPaginationHelper,
                new TransactionDatePaginationController<>(transactionFetcher),
                transactionFetcher);
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new DanskeBankSessionHandler(this.apiClient, this.configuration);
    }
}
