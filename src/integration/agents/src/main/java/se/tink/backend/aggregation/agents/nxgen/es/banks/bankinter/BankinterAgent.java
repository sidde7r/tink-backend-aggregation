package se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CREDIT_CARDS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.IDENTITY_DATA;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.INVESTMENTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.LOANS;

import com.google.inject.Inject;
import java.util.concurrent.TimeUnit;
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
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.BankinterConstants.HeaderValues;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.authenticator.BankinterAuthenticationClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.authenticator.BankinterAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.authenticator.HtmlLogger;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.fetcher.creditcard.BankinterCreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.fetcher.identitydata.BankinterIdentityDataFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.fetcher.investment.BankinterInvestmentFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.fetcher.loan.BankinterLoanFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.fetcher.transactionalaccount.BankinterTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.session.BankinterSessionHandler;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.identitydata.IdentityDataFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.investment.InvestmentRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.loan.LoanRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.storage.AgentTemporaryStorage;
import se.tink.integration.webdriver.ChromeDriverConfig;
import se.tink.integration.webdriver.ChromeDriverInitializer;
import se.tink.integration.webdriver.WebDriverWrapper;

@AgentCapabilities({CHECKING_ACCOUNTS, CREDIT_CARDS, INVESTMENTS, IDENTITY_DATA, LOANS})
public final class BankinterAgent extends NextGenerationAgent
        implements RefreshIdentityDataExecutor,
                RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor,
                RefreshInvestmentAccountsExecutor,
                RefreshCreditCardAccountsExecutor,
                RefreshLoanAccountsExecutor {

    private final BankinterApiClient apiClient;
    private final AgentTemporaryStorage agentTemporaryStorage;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private final InvestmentRefreshController investmentRefreshController;
    private final CreditCardRefreshController creditCardRefreshController;
    private final LoanRefreshController loanRefreshController;

    @Inject
    public BankinterAgent(AgentComponentProvider agentComponentProvider) {
        super(agentComponentProvider);
        apiClient = new BankinterApiClient(client);
        agentTemporaryStorage = agentComponentProvider.getAgentTemporaryStorage();
        transactionalAccountRefreshController = constructTransactionalAccountRefreshController();
        investmentRefreshController = constructInvestmentRefreshController();
        creditCardRefreshController = constructCreditCardRefreshController();
        loanRefreshController = constructLoanRefreshController();
        setupHttpClient();
    }

    @Override
    protected Authenticator constructAuthenticator() {
        WebDriverWrapper driver =
                ChromeDriverInitializer.constructChromeDriver(
                        ChromeDriverConfig.builder()
                                .userAgent(HeaderValues.USER_AGENT)
                                .acceptLanguage(HeaderValues.ACCEPT_LANGUAGE)
                                .build(),
                        agentTemporaryStorage);
        driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
        BankinterAuthenticationClient authenticationClient =
                new BankinterAuthenticationClient(
                        driver,
                        agentTemporaryStorage,
                        new HtmlLogger(driver, context.getLogOutputStream()),
                        apiClient);
        BankinterAuthenticator authenticator =
                new BankinterAuthenticator(supplementalInformationHelper, authenticationClient);
        return new PasswordAuthenticationController(authenticator);
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
        final BankinterTransactionalAccountFetcher accountFetcher =
                new BankinterTransactionalAccountFetcher(apiClient);

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                accountFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionKeyPaginationController<>(accountFetcher)));
    }

    private InvestmentRefreshController constructInvestmentRefreshController() {
        final BankinterInvestmentFetcher investmentFetcher =
                new BankinterInvestmentFetcher(apiClient);
        return new InvestmentRefreshController(
                metricRefreshController,
                updateController,
                investmentFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionKeyPaginationController<>(investmentFetcher)));
    }

    private CreditCardRefreshController constructCreditCardRefreshController() {
        final BankinterCreditCardFetcher creditCardFetcher =
                new BankinterCreditCardFetcher(apiClient);

        return new CreditCardRefreshController(
                metricRefreshController,
                updateController,
                creditCardFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionKeyPaginationController<>(creditCardFetcher)));
    }

    private LoanRefreshController constructLoanRefreshController() {
        final BankinterLoanFetcher loanFetcher = new BankinterLoanFetcher(apiClient);

        return new LoanRefreshController(
                metricRefreshController,
                updateController,
                loanFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionKeyPaginationController<>(loanFetcher)));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new BankinterSessionHandler(apiClient);
    }

    @Override
    public FetchIdentityDataResponse fetchIdentityData() {
        final IdentityDataFetcher fetcher = new BankinterIdentityDataFetcher(apiClient);
        return new FetchIdentityDataResponse(fetcher.fetchIdentityData());
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

    @Override
    public FetchLoanAccountsResponse fetchLoanAccounts() {
        return loanRefreshController.fetchLoanAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchLoanTransactions() {
        return loanRefreshController.fetchLoanTransactions();
    }

    private void setupHttpClient() {
        client.setFollowRedirects(false);
    }
}
