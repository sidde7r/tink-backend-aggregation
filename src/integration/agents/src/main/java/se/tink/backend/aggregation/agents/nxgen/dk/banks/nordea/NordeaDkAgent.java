package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea;

import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.CREDIT_CARDS;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.IDENTITY_DATA;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.INVESTMENTS;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.LOANS;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.MORTGAGE_AGGREGATION;

import com.google.inject.Inject;
import java.time.temporal.ChronoUnit;
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
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.contexts.StatusUpdater;
import se.tink.backend.aggregation.agents.module.annotation.AgentDependencyModules;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.authenticator.NordeaDkAuthenticatorUtils;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.authenticator.NordeaDkAutoAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.authenticator.NordeaDkMitIdAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.authenticator.NordeaDkNemIdAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.creditcard.NordeaCreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.creditcard.NordeaCreditCardTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.identitydata.NordeaDKIdentityDataFetcher;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.investment.NordeaInvestmentFetcher;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.loans.NordeaDkLoansFetcher;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.transactionalaccount.NordeaAccountFetcher;
import se.tink.backend.aggregation.logmasker.LogMasker;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.DkSSAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.DkSSAuthenticatorsConfig;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.DkSSMethod;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.MitIdAuthenticationControllerProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.MitIdAuthenticationControllerProviderModule;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.NemIdIFrameControllerInitializer;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.NemIdIFrameControllerInitializerModule;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.investment.InvestmentRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.loan.LoanRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.http.client.LoggingStrategy;
import se.tink.backend.aggregation.nxgen.storage.AgentTemporaryStorage;
import se.tink.libraries.credentials.service.UserAvailability;

@AgentCapabilities({
    CHECKING_ACCOUNTS,
    CREDIT_CARDS,
    INVESTMENTS,
    LOANS,
    MORTGAGE_AGGREGATION,
    IDENTITY_DATA
})
@AgentDependencyModules(
        modules = {
            NemIdIFrameControllerInitializerModule.class,
            MitIdAuthenticationControllerProviderModule.class
        })
public final class NordeaDkAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor,
                RefreshCreditCardAccountsExecutor,
                RefreshInvestmentAccountsExecutor,
                RefreshLoanAccountsExecutor,
                RefreshIdentityDataExecutor {

    private final NemIdIFrameControllerInitializer iFrameControllerInitializer;
    private final MitIdAuthenticationControllerProvider mitIdAuthenticationControllerProvider;
    private final UserAvailability userAvailability;
    private final NordeaDkApiClient nordeaClient;
    private final AgentTemporaryStorage agentTemporaryStorage;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private final CreditCardRefreshController creditCardRefreshController;
    private final InvestmentRefreshController investmentRefreshController;
    private final LoanRefreshController loanRefreshController;
    private final StatusUpdater statusUpdater;
    private final LogMasker logMasker;

    @Inject
    public NordeaDkAgent(
            AgentComponentProvider agentComponentProvider,
            NemIdIFrameControllerInitializer iFrameControllerInitializer,
            MitIdAuthenticationControllerProvider mitIdAuthenticationControllerProvider) {
        super(agentComponentProvider);

        setJsonHttpTrafficLogsEnabled(true);
        client.setLoggingStrategy(LoggingStrategy.EXPERIMENTAL);

        this.logMasker = agentComponentProvider.getContext().getLogMasker();
        this.iFrameControllerInitializer = iFrameControllerInitializer;
        this.mitIdAuthenticationControllerProvider = mitIdAuthenticationControllerProvider;
        this.userAvailability =
                agentComponentProvider.getCredentialsRequest().getUserAvailability();
        this.nordeaClient = constructNordeaClient();
        this.agentTemporaryStorage = agentComponentProvider.getAgentTemporaryStorage();
        this.transactionalAccountRefreshController =
                constructTransactionalAccountRefreshController();
        this.creditCardRefreshController = constructCreditCardRefreshController();
        this.investmentRefreshController = contructInvestmentRefreshController();
        this.loanRefreshController = constructLoanRefreshController();
        statusUpdater = agentComponentProvider.getContext();
    }

    @Override
    protected Authenticator constructAuthenticator() {
        NordeaDkAuthenticatorUtils authenticatorUtils =
                new NordeaDkAuthenticatorUtils(sessionStorage, persistentStorage, nordeaClient);

        DkSSAuthenticationController manualAuthenticator =
                new DkSSAuthenticationController(
                        DkSSAuthenticatorsConfig.builder()
                                .addProvider(
                                        DkSSMethod.NEM_ID,
                                        () ->
                                                new NordeaDkNemIdAuthenticator(
                                                        nordeaClient,
                                                        sessionStorage,
                                                        catalog,
                                                        statusUpdater,
                                                        supplementalInformationController,
                                                        metricContext,
                                                        agentTemporaryStorage,
                                                        iFrameControllerInitializer,
                                                        authenticatorUtils))
                                .addProvider(
                                        DkSSMethod.MIT_ID,
                                        () -> {
                                            NordeaDkMitIdAuthenticator mitIdAuthenticator =
                                                    new NordeaDkMitIdAuthenticator(
                                                            authenticatorUtils, nordeaClient);
                                            return mitIdAuthenticationControllerProvider
                                                    .createAuthController(
                                                            catalog,
                                                            statusUpdater,
                                                            supplementalInformationController,
                                                            userAvailability,
                                                            agentTemporaryStorage,
                                                            mitIdAuthenticator);
                                        })
                                .build());

        NordeaDkAutoAuthenticator autoAuthenticator =
                new NordeaDkAutoAuthenticator(nordeaClient, persistentStorage, authenticatorUtils);

        return new AutoAuthenticationController(
                request, systemUpdater, manualAuthenticator, autoAuthenticator);
    }

    private InvestmentRefreshController contructInvestmentRefreshController() {
        NordeaInvestmentFetcher fetcher = new NordeaInvestmentFetcher(nordeaClient);
        return new InvestmentRefreshController(metricRefreshController, updateController, fetcher);
    }

    private CreditCardRefreshController constructCreditCardRefreshController() {
        return new CreditCardRefreshController(
                metricRefreshController,
                updateController,
                new NordeaCreditCardFetcher(nordeaClient),
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionKeyPaginationController<>(
                                new NordeaCreditCardTransactionFetcher(nordeaClient))));
    }

    private TransactionalAccountRefreshController constructTransactionalAccountRefreshController() {
        NordeaAccountFetcher fetcher = new NordeaAccountFetcher(nordeaClient);
        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                fetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionDatePaginationController.Builder<>(fetcher)
                                .setConsecutiveEmptyPagesLimit(2)
                                .setAmountAndUnitToFetch(12, ChronoUnit.MONTHS)
                                .build()));
    }

    private LoanRefreshController constructLoanRefreshController() {
        NordeaDkLoansFetcher fetcher = new NordeaDkLoansFetcher(nordeaClient);
        return new LoanRefreshController(
                metricRefreshController,
                updateController,
                fetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionKeyPaginationController<>(fetcher)));
    }

    private NordeaDkApiClient constructNordeaClient() {
        return new NordeaDkApiClient(sessionStorage, client, persistentStorage, catalog, logMasker);
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
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
        return new FetchTransactionsResponse(Collections.emptyMap());
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
    public FetchIdentityDataResponse fetchIdentityData() {
        return new FetchIdentityDataResponse(
                new NordeaDKIdentityDataFetcher(nordeaClient).fetchIdentityData());
    }
}
