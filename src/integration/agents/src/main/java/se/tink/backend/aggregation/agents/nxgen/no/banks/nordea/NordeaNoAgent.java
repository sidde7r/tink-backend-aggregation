package se.tink.backend.aggregation.agents.nxgen.no.banks.nordea;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CREDIT_CARDS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.LOANS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.MORTGAGE_AGGREGATION;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.FetchLoanAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshCreditCardAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshIdentityDataExecutor;
import se.tink.backend.aggregation.agents.RefreshLoanAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.module.annotation.AgentDependencyModules;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.authenticator.NordeaBankIdIFrameInitializer;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.authenticator.NordeaNoAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.authenticator.NordeaNoSessionHandler;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.client.AuthenticationClient;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.client.BaseClient;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.client.ExceptionFilter;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.client.FetcherClient;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.fetcher.creditcard.CreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.fetcher.creditcard.CreditCardTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.fetcher.identitydata.IdentityDataFetcher;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.fetcher.investment.InvestmentFetcher;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.fetcher.loan.LoanFetcher;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.fetcher.transactionalaccount.TransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.fetcher.transactionalaccount.TransactionalAccountTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.filters.NordeaNoRetryFilter;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdIframeAuthenticationControllerProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdIframeAuthenticationControllerProviderModule;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.investment.InvestmentRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.loan.LoanRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.libraries.credentials.service.UserAvailability;

@AgentCapabilities({CHECKING_ACCOUNTS, SAVINGS_ACCOUNTS, CREDIT_CARDS, LOANS, MORTGAGE_AGGREGATION})
@AgentDependencyModules(modules = BankIdIframeAuthenticationControllerProviderModule.class)
public final class NordeaNoAgent extends NextGenerationAgent
        implements RefreshIdentityDataExecutor,
                RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor,
                RefreshCreditCardAccountsExecutor,
                RefreshLoanAccountsExecutor {

    private final BankIdIframeAuthenticationControllerProvider authenticationControllerProvider;

    private final NordeaNoStorage storage;
    private final RandomValueGenerator randomValueGenerator;
    private final UserAvailability userAvailability;

    private final AuthenticationClient authenticationClient;
    private final FetcherClient fetcherClient;

    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private final CreditCardRefreshController creditCardRefreshController;
    private final InvestmentRefreshController investmentRefreshController;
    private final LoanRefreshController loanRefreshController;

    @Inject
    public NordeaNoAgent(
            AgentComponentProvider componentProvider,
            BankIdIframeAuthenticationControllerProvider authenticationControllerProvider) {
        super(componentProvider);
        this.authenticationControllerProvider = authenticationControllerProvider;

        this.storage = new NordeaNoStorage(persistentStorage, sessionStorage);
        this.randomValueGenerator = componentProvider.getRandomValueGenerator();
        this.userAvailability = componentProvider.getCredentialsRequest().getUserAvailability();

        TinkHttpClient httpClient = componentProvider.getTinkHttpClient();
        httpClient.addFilter(new ExceptionFilter());
        httpClient.addFilter(
                new NordeaNoRetryFilter(
                        NordeaNoConstants.RetryFilter.NUM_TIMEOUT_RETRIES,
                        NordeaNoConstants.RetryFilter.RETRY_SLEEP_MILLISECONDS));

        BaseClient baseClient = new BaseClient(httpClient, storage);
        this.authenticationClient = new AuthenticationClient(baseClient, storage);
        this.fetcherClient = new FetcherClient(baseClient);

        transactionalAccountRefreshController = constructTransactionalAccountRefreshController();
        creditCardRefreshController = constructCreditCardRefreshController();

        investmentRefreshController =
                new InvestmentRefreshController(
                        metricRefreshController,
                        updateController,
                        new InvestmentFetcher(fetcherClient));
        loanRefreshController =
                new LoanRefreshController(
                        metricRefreshController, updateController, new LoanFetcher(fetcherClient));
    }

    @Override
    protected Authenticator constructAuthenticator() {
        NordeaBankIdIFrameInitializer iframeInitializer =
                new NordeaBankIdIFrameInitializer(
                        authenticationClient, storage, randomValueGenerator);

        NordeaNoAuthenticator authenticator =
                new NordeaNoAuthenticator(authenticationClient, storage);

        return authenticationControllerProvider.createAuthController(
                catalog,
                context,
                supplementalInformationController,
                iframeInitializer,
                authenticator,
                userAvailability);
    }

    private TransactionalAccountRefreshController constructTransactionalAccountRefreshController() {
        TransactionalAccountFetcher accountFetcher = new TransactionalAccountFetcher(fetcherClient);
        TransactionalAccountTransactionFetcher transactionFetcher =
                new TransactionalAccountTransactionFetcher(fetcherClient);
        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                accountFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionKeyPaginationController<>(transactionFetcher)));
    }

    private CreditCardRefreshController constructCreditCardRefreshController() {
        CreditCardFetcher creditCardFetcher = new CreditCardFetcher(fetcherClient);
        CreditCardTransactionFetcher creditCardTransactionFetcher =
                new CreditCardTransactionFetcher(fetcherClient);
        return new CreditCardRefreshController(
                metricRefreshController,
                updateController,
                creditCardFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionPagePaginationController<>(
                                creditCardTransactionFetcher, 1)));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new NordeaNoSessionHandler(authenticationClient, storage);
    }

    @Override
    public FetchIdentityDataResponse fetchIdentityData() {
        return new FetchIdentityDataResponse(
                new IdentityDataFetcher(fetcherClient).fetchIdentityData());
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
    public FetchAccountsResponse fetchCreditCardAccounts() {
        return creditCardRefreshController.fetchCreditCardAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchCreditCardTransactions() {
        return creditCardRefreshController.fetchCreditCardTransactions();
    }

    //    Investments are temporarly disabled for Norwegian Agents ITE-1676
    //
    //    @Override
    //    public FetchInvestmentAccountsResponse fetchInvestmentAccounts() {
    //        return investmentRefreshController.fetchInvestmentAccounts();
    //    }
    //
    //    @Override
    //    public FetchTransactionsResponse fetchInvestmentTransactions() {
    //        return investmentRefreshController.fetchInvestmentTransactions();
    //    }

    @Override
    public FetchLoanAccountsResponse fetchLoanAccounts() {
        return loanRefreshController.fetchLoanAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchLoanTransactions() {
        return loanRefreshController.fetchLoanTransactions();
    }
}
