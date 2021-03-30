package se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar;

import static se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.LunarConstants.HeaderValues;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.IDENTITY_DATA;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.INVESTMENTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.LOANS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;

import com.google.inject.Inject;
import java.time.Clock;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.FetchInvestmentAccountsResponse;
import se.tink.backend.aggregation.agents.FetchLoanAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshIdentityDataExecutor;
import se.tink.backend.aggregation.agents.RefreshInvestmentAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshLoanAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.agentplatform.AgentPlatformHttpClient;
import se.tink.backend.aggregation.agents.agentplatform.authentication.AgentPlatformAgent;
import se.tink.backend.aggregation.agents.agentplatform.authentication.AgentPlatformAuthenticator;
import se.tink.backend.aggregation.agents.agentplatform.authentication.ObjectMapperFactory;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.LunarAuthenticationConfig;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.LunarNemIdParametersFetcher;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.NemIdIframeAttributes;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.client.AuthenticationApiClient;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.persistance.LunarDataAccessorFactory;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.client.FetcherApiClient;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.investment.LunarInvestmentsFetcher;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.loan.LunarLoansFetcher;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.transactionalaccount.LunarIdentityDataFetcher;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.transactionalaccount.LunarTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.transactionalaccount.LunarTransactionalAccountFetcher;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcess;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.investment.InvestmentRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.loan.LoanRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filters.retry.TimeoutRetryFilter;

@AgentCapabilities({CHECKING_ACCOUNTS, SAVINGS_ACCOUNTS, LOANS, INVESTMENTS, IDENTITY_DATA})
public final class LunarDkAgent extends AgentPlatformAgent
        implements RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor,
                RefreshLoanAccountsExecutor,
                RefreshInvestmentAccountsExecutor,
                RefreshIdentityDataExecutor,
                AgentPlatformAuthenticator {

    private final FetcherApiClient apiClient;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private final LoanRefreshController loanRefreshController;
    private final InvestmentRefreshController investmentRefreshController;
    private final LunarAuthenticationConfig lunarAuthenticationConfig;
    private final RandomValueGenerator randomValueGenerator;
    private final LunarDataAccessorFactory accessorFactory;
    private final LunarIdentityDataFetcher identityDataFetcher;

    @Inject
    public LunarDkAgent(AgentComponentProvider agentComponentProvider) {
        super(agentComponentProvider);
        configureHttpClient(client);
        randomValueGenerator = agentComponentProvider.getRandomValueGenerator();
        accessorFactory = new LunarDataAccessorFactory(new ObjectMapperFactory().getInstance());
        String languageCode = HeaderValues.getLanguageCode(request.getUser().getLocale());

        this.apiClient =
                new FetcherApiClient(
                        client,
                        getPersistentStorage(),
                        accessorFactory,
                        randomValueGenerator,
                        languageCode);

        AuthenticationApiClient authenticationApiClient =
                new AuthenticationApiClient(
                        new AgentPlatformHttpClient(client), randomValueGenerator, languageCode);

        this.identityDataFetcher =
                new LunarIdentityDataFetcher(apiClient, accessorFactory, getPersistentStorage());

        this.transactionalAccountRefreshController =
                constructTransactionalAccountRefreshController();

        this.loanRefreshController = constructLoanRefreshController();

        this.investmentRefreshController = constructInvestmentRefreshController();

        this.lunarAuthenticationConfig =
                createLunarAuthenticationConfig(agentComponentProvider, authenticationApiClient);
    }

    private TransactionalAccountRefreshController constructTransactionalAccountRefreshController() {
        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                new LunarTransactionalAccountFetcher(
                        apiClient, accessorFactory, persistentStorage, identityDataFetcher),
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionKeyPaginationController<>(
                                new LunarTransactionFetcher(apiClient))));
    }

    private LoanRefreshController constructLoanRefreshController() {
        LunarLoansFetcher lunarLoansFetcher = new LunarLoansFetcher(apiClient, identityDataFetcher);
        return new LoanRefreshController(
                metricRefreshController,
                updateController,
                lunarLoansFetcher,
                new TransactionFetcherController<>(transactionPaginationHelper, lunarLoansFetcher));
    }

    private InvestmentRefreshController constructInvestmentRefreshController() {
        return new InvestmentRefreshController(
                metricRefreshController,
                updateController,
                new LunarInvestmentsFetcher(apiClient, identityDataFetcher));
    }

    private LunarAuthenticationConfig createLunarAuthenticationConfig(
            AgentComponentProvider agentComponentProvider,
            AuthenticationApiClient authenticationApiClient) {
        LunarNemIdParametersFetcher parametersFetcher =
                new LunarNemIdParametersFetcher(Clock.systemDefaultZone());
        return new LunarAuthenticationConfig(
                authenticationApiClient,
                accessorFactory,
                randomValueGenerator,
                new NemIdIframeAttributes(
                        parametersFetcher,
                        catalog,
                        agentComponentProvider.getContext(),
                        supplementalInformationController,
                        metricContext,
                        credentials));
    }

    protected void configureHttpClient(TinkHttpClient client) {
        client.addFilter(
                new TimeoutRetryFilter(
                        LunarConstants.HttpClient.MAX_RETRIES,
                        LunarConstants.HttpClient.RETRY_SLEEP_MILLISECONDS));
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
    public FetchLoanAccountsResponse fetchLoanAccounts() {
        return loanRefreshController.fetchLoanAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchLoanTransactions() {
        return loanRefreshController.fetchLoanTransactions();
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
    public FetchIdentityDataResponse fetchIdentityData() {
        return new FetchIdentityDataResponse(identityDataFetcher.fetchIdentityData());
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }

    @Override
    public AgentAuthenticationProcess getAuthenticationProcess() {
        return lunarAuthenticationConfig.createAuthProcess();
    }

    @Override
    public boolean isBackgroundRefreshPossible() {
        return true;
    }
}
