package se.tink.backend.aggregation.agents.nxgen.no.banks.nordeapoc;

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
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordeapoc.authenticator.NordeaNoAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordeapoc.client.AuthenticationClient;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordeapoc.client.BaseClient;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordeapoc.client.ExceptionFilter;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordeapoc.client.FetcherClient;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordeapoc.fetcher.creditcard.CreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordeapoc.fetcher.creditcard.CreditCardTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordeapoc.fetcher.identitydata.IdentityDataFetcher;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordeapoc.fetcher.investment.InvestmentFetcher;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordeapoc.fetcher.loan.LoanFetcher;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordeapoc.fetcher.transactionalaccount.TransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordeapoc.fetcher.transactionalaccount.TransactionalAccountTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordeapoc.filters.NordeaNoRetryFilter;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGeneratorImpl;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.investment.InvestmentRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.loan.LoanRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.libraries.credentials.service.CredentialsRequest;
import src.integration.bankid.BankIdOidcIframeAuthenticationService;

@AgentCapabilities({CHECKING_ACCOUNTS, SAVINGS_ACCOUNTS, CREDIT_CARDS, LOANS, MORTGAGE_AGGREGATION})
public final class NordeaNoPocAgent extends NextGenerationAgent
        implements RefreshIdentityDataExecutor,
                RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor,
                RefreshCreditCardAccountsExecutor,
                RefreshLoanAccountsExecutor {

    private final NordeaNoStorage storage;

    private final AuthenticationClient authenticationClient;
    private final FetcherClient fetcherClient;

    private final BankIdOidcIframeAuthenticationService bankIdOidcService;

    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private final CreditCardRefreshController creditCardRefreshController;
    private final InvestmentRefreshController investmentRefreshController;
    private final LoanRefreshController loanRefreshController;

    @Inject
    public NordeaNoPocAgent(
            CredentialsRequest credentialsRequest,
            AgentContext agentContext,
            AgentComponentProvider componentProvider) {
        super(componentProvider);
        this.storage = new NordeaNoStorage(persistentStorage);

        TinkHttpClient httpClient = componentProvider.getTinkHttpClient();
        httpClient.addFilter(new ExceptionFilter());
        httpClient.addFilter(
                new NordeaNoRetryFilter(
                        NordeaNoConstants.RetryFilter.NUM_TIMEOUT_RETRIES,
                        NordeaNoConstants.RetryFilter.RETRY_SLEEP_MILLISECONDS));
        httpClient.setFollowRedirects(false);

        BaseClient baseClient = new BaseClient(httpClient, storage);
        this.authenticationClient =
                new AuthenticationClient(baseClient, storage, agentContext.isTestContext());
        this.fetcherClient = new FetcherClient(baseClient);

        this.bankIdOidcService =
                new BankIdOidcIframeAuthenticationService(
                        supplementalInformationHelper,
                        strongAuthenticationState,
                        getTinkApiUrl(credentialsRequest),
                        agentContext.isTestContext());

        transactionalAccountRefreshController = constructTransactionalAccountRefreshController();
        creditCardRefreshController = constructCreditCardRefreshController();
        investmentRefreshController = constructInvestmentRefreshController();
        loanRefreshController = constructLoanRefreshController();
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new NordeaNoAuthenticator(
                authenticationClient, storage, new RandomValueGeneratorImpl(), bankIdOidcService);
    }

    private String getTinkApiUrl(CredentialsRequest credentialsRequest) {
        return credentialsRequest.getProvider().getPayload();
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

    private InvestmentRefreshController constructInvestmentRefreshController() {
        return new InvestmentRefreshController(
                metricRefreshController, updateController, new InvestmentFetcher(fetcherClient));
    }

    private LoanRefreshController constructLoanRefreshController() {
        return new LoanRefreshController(
                metricRefreshController, updateController, new LoanFetcher(fetcherClient));
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
