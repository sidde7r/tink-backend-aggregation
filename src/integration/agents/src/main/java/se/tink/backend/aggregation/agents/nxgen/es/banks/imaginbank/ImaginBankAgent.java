package se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank;

import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.CREDIT_CARDS;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.IDENTITY_DATA;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.SAVINGS_ACCOUNTS;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.FetchInvestmentAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshCreditCardAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshIdentityDataExecutor;
import se.tink.backend.aggregation.agents.RefreshInvestmentAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.ImaginBankConstants.RetryFilterValues;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.authenticator.ImaginBankPasswordAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.fetcher.creditcard.ImaginBankCreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.fetcher.identitydata.ImaginBankIdentityDataFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.fetcher.investment.ImaginBankInvestmentFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.fetcher.transactionalaccount.ImaginBankAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.fetcher.transactionalaccount.ImaginBankTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.filter.ImaginBankRetryFilter;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.session.ImaginBankSessionHandler;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.investment.InvestmentRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filters.TimeoutFilter;

/*
 * This agent is to great extents a copy of lacaixa agent.
 * The main differences are authentication.
 * ImaginBank also has separate account fetching
 */
@AgentCapabilities({CHECKING_ACCOUNTS, SAVINGS_ACCOUNTS, CREDIT_CARDS, IDENTITY_DATA})
public final class ImaginBankAgent extends NextGenerationAgent
        implements RefreshIdentityDataExecutor,
                RefreshCreditCardAccountsExecutor,
                RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor,
                RefreshInvestmentAccountsExecutor {

    private final ImaginBankApiClient apiClient;
    private final ImaginBankSessionStorage imaginBankSessionStorage;

    private final CreditCardRefreshController creditCardRefreshController;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private final InvestmentRefreshController investmentRefreshController;

    @Inject
    public ImaginBankAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);
        configureHttpClient(client);
        imaginBankSessionStorage = new ImaginBankSessionStorage(sessionStorage);
        apiClient = new ImaginBankApiClient(client, persistentStorage, imaginBankSessionStorage);

        creditCardRefreshController = constructCreditCardRefreshController();
        transactionalAccountRefreshController = constructTransactionalAccountRefreshController();
        investmentRefreshController = constructInvestmentController();
    }

    private void configureHttpClient(TinkHttpClient client) {
        client.addFilter(new TimeoutFilter());
        client.addFilter(
                new ImaginBankRetryFilter(
                        RetryFilterValues.MAX_ATTEMPTS,
                        RetryFilterValues.RETRY_SLEEP_MILLISECONDS));
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new ImaginBankPasswordAuthenticator(
                apiClient, imaginBankSessionStorage, supplementalInformationHelper);
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
        ImaginBankAccountFetcher accountFetcher = new ImaginBankAccountFetcher(apiClient);
        ImaginBankTransactionFetcher transactionFetcher =
                new ImaginBankTransactionFetcher(apiClient);

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                accountFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionPagePaginationController<>(transactionFetcher, 0)));
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
    public FetchInvestmentAccountsResponse fetchInvestmentAccounts() {
        return investmentRefreshController.fetchInvestmentAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchInvestmentTransactions() {
        return investmentRefreshController.fetchInvestmentTransactions();
    }

    private CreditCardRefreshController constructCreditCardRefreshController() {
        ImaginBankCreditCardFetcher creditCardFetcher = new ImaginBankCreditCardFetcher(apiClient);

        return new CreditCardRefreshController(
                metricRefreshController,
                updateController,
                creditCardFetcher,
                new TransactionFetcherController<>(
                        this.transactionPaginationHelper,
                        new TransactionPagePaginationController<>(creditCardFetcher, 0)));
    }

    private InvestmentRefreshController constructInvestmentController() {
        return new InvestmentRefreshController(
                metricRefreshController,
                updateController,
                new ImaginBankInvestmentFetcher(apiClient));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new ImaginBankSessionHandler(apiClient);
    }

    @Override
    public FetchIdentityDataResponse fetchIdentityData() {
        final ImaginBankIdentityDataFetcher fetcher =
                new ImaginBankIdentityDataFetcher(imaginBankSessionStorage, apiClient);
        return new FetchIdentityDataResponse(fetcher.fetchIdentityData());
    }
}
