package se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CREDIT_CARDS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.IDENTITY_DATA;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.INVESTMENTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.LOANS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;

import com.google.inject.Inject;
import se.tink.backend.agents.rpc.Field;
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
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.authenticator.NordeaCodesAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.fetcher.creditcard.NordeaCreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.fetcher.creditcard.NordeaCreditCardTransactionsFetcher;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.fetcher.identitydata.NordeaFIIdentityDataFetcher;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.fetcher.investment.NordeaInvestmentFetcher;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.fetcher.loan.NordeaLoanFetcher;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.fetcher.transactionalaccount.NordeaTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.fetcher.transactionalaccount.NordeaTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.filters.NordeaFIRetryFilter;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.session.NordeaFISessionHandler;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.investment.InvestmentRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.loan.LoanRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filters.ServiceUnavailableBankServiceErrorFilter;

@AgentCapabilities({
    CHECKING_ACCOUNTS,
    SAVINGS_ACCOUNTS,
    CREDIT_CARDS,
    INVESTMENTS,
    IDENTITY_DATA,
    LOANS
})
public final class NordeaFIAgent extends NextGenerationAgent
        implements RefreshIdentityDataExecutor,
                RefreshInvestmentAccountsExecutor,
                RefreshLoanAccountsExecutor,
                RefreshCreditCardAccountsExecutor,
                RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor {
    private final NordeaFIApiClient apiClient;
    private final InvestmentRefreshController investmentRefreshController;
    private final LoanRefreshController loanRefreshController;
    private final CreditCardRefreshController creditCardRefreshController;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;

    @Inject
    public NordeaFIAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);
        configureHttpClient(client);
        apiClient = new NordeaFIApiClient(client, sessionStorage);

        NordeaInvestmentFetcher investmentFetcher = new NordeaInvestmentFetcher(apiClient);
        investmentRefreshController =
                new InvestmentRefreshController(
                        metricRefreshController, updateController, investmentFetcher);

        NordeaLoanFetcher loanFetcher = new NordeaLoanFetcher(apiClient);
        loanRefreshController =
                new LoanRefreshController(metricRefreshController, updateController, loanFetcher);

        creditCardRefreshController = constructCreditCardRefreshController();

        transactionalAccountRefreshController = constructTransactionalAccountRefreshController();
    }

    private void configureHttpClient(TinkHttpClient client) {
        client.setTimeout(NordeaFIConstants.HttpClient.TIMEOUT_MS);
        client.addFilter(new ServiceUnavailableBankServiceErrorFilter());
        client.addFilter(
                new NordeaFIRetryFilter(
                        NordeaFIConstants.HttpClient.MAX_RETRIES,
                        NordeaFIConstants.HttpClient.RETRY_SLEEP_MS));
    }

    @Override
    protected Authenticator constructAuthenticator() {
        NordeaCodesAuthenticator codesAuthenticator =
                new NordeaCodesAuthenticator(
                        apiClient, sessionStorage, credentials.getField(Field.Key.USERNAME));
        return new ThirdPartyAppAuthenticationController<>(
                codesAuthenticator, supplementalInformationHelper);
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
        NordeaTransactionalAccountFetcher accountFetcher =
                new NordeaTransactionalAccountFetcher(apiClient);
        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                accountFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionKeyPaginationController<>(
                                new NordeaTransactionFetcher(apiClient))));
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
        NordeaCreditCardFetcher creditCardFetcher = new NordeaCreditCardFetcher(apiClient);
        return new CreditCardRefreshController(
                metricRefreshController,
                updateController,
                creditCardFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionPagePaginationController<>(
                                new NordeaCreditCardTransactionsFetcher(apiClient),
                                NordeaFIConstants.Fetcher.START_PAGE)));
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
        return new NordeaFISessionHandler(apiClient);
    }

    @Override
    public FetchIdentityDataResponse fetchIdentityData() {
        return new FetchIdentityDataResponse(
                new NordeaFIIdentityDataFetcher(apiClient).fetchIdentityData());
    }
}
