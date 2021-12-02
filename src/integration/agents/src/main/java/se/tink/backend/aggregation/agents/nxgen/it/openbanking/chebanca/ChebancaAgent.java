package se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.module.annotation.AgentDependencyModules;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.ChebancaConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.authenticator.ChebancaAuthenticationController;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.authenticator.ChebancaBgAutoAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.authenticator.ChebancaConsentManager;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.authenticator.ChebancaOAuth2Authenticator;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.configuration.ChebancaConfiguration;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.detail.ChebancaRequestBuilder;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.ChebancaTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.ChebancaTransactionalAccountFetcher;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;
import se.tink.backend.aggregation.eidassigner.SignatureHeaderGenerator;
import se.tink.backend.aggregation.eidassigner.module.QSealcSignerModuleRSASHA256;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

@AgentDependencyModules(modules = QSealcSignerModuleRSASHA256.class)
@AgentCapabilities({CHECKING_ACCOUNTS, SAVINGS_ACCOUNTS})
public final class ChebancaAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor, RefreshSavingsAccountsExecutor {

    private final ChebancaApiClient apiClient;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private AgentConfiguration<ChebancaConfiguration> agentConfiguration;

    @Inject
    public ChebancaAgent(AgentComponentProvider agentComponentProvider, QsealcSigner qSealcSigner) {
        super(agentComponentProvider);

        agentConfiguration =
                getAgentConfigurationController()
                        .getAgentConfiguration(ChebancaConfiguration.class);

        boolean isUserAvailable = request.getUserAvailability().isUserAvailableForInteraction();
        ChebancaRequestBuilder chebancaRequestBuilder =
                createChebancaRequestBuilder(
                        isUserAvailable,
                        qSealcSigner,
                        agentComponentProvider.getRandomValueGenerator());
        apiClient =
                new ChebancaApiClient(
                        persistentStorage,
                        strongAuthenticationState,
                        agentConfiguration.getRedirectUrl(),
                        isUserAvailable,
                        chebancaRequestBuilder);

        client.setFollowRedirects(false);
        this.transactionalAccountRefreshController =
                getTransactionalAccountRefreshController(
                        agentComponentProvider.getLocalDateTimeSource());
    }

    private ChebancaRequestBuilder createChebancaRequestBuilder(
            boolean isUserAvailable,
            QsealcSigner qsealcSigner,
            RandomValueGenerator randomValueGenerator) {

        ChebancaConfiguration configuration = agentConfiguration.getProviderSpecificConfiguration();
        String applicationId =
                isUserAvailable
                        ? configuration.getManualRefreshApplicationId()
                        : configuration.getAutoRefreshApplicationId();

        return new ChebancaRequestBuilder(
                client,
                new SignatureHeaderGenerator(
                        ChebancaConstants.HeaderValues.SIGNATURE_HEADER,
                        HeaderKeys.HEADERS_TO_SIGN,
                        applicationId,
                        qsealcSigner),
                randomValueGenerator);
    }

    @Override
    public void setConfiguration(AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);
        client.setEidasProxy(configuration.getEidasProxy());
    }

    @Override
    protected Authenticator constructAuthenticator() {
        ChebancaConsentManager consentManager =
                new ChebancaConsentManager(
                        apiClient,
                        supplementalInformationHelper,
                        strongAuthenticationState,
                        credentials);

        ChebancaAuthenticationController authenticationController =
                new ChebancaAuthenticationController(
                        constructOAuth2Authenticator(),
                        consentManager,
                        supplementalInformationHelper,
                        constructBgRefreshAuthenticator(),
                        request.getUserAvailability().isUserAvailableForInteraction());

        return new AutoAuthenticationController(
                request, systemUpdater, authenticationController, authenticationController);
    }

    private ChebancaBgAutoAuthenticator constructBgRefreshAuthenticator() {
        return new ChebancaBgAutoAuthenticator(apiClient, agentConfiguration, persistentStorage);
    }

    private OAuth2AuthenticationController constructOAuth2Authenticator() {
        return new OAuth2AuthenticationController(
                persistentStorage,
                supplementalInformationHelper,
                new ChebancaOAuth2Authenticator(
                        apiClient, agentConfiguration, strongAuthenticationState),
                credentials,
                strongAuthenticationState);
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

    private TransactionalAccountRefreshController getTransactionalAccountRefreshController(
            LocalDateTimeSource localDateTimeSource) {
        final ChebancaTransactionalAccountFetcher accountFetcher =
                new ChebancaTransactionalAccountFetcher(apiClient);

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                accountFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionDatePaginationController.Builder<>(
                                        new ChebancaTransactionFetcher(apiClient))
                                .setLocalDateTimeSource(localDateTimeSource)
                                .build()));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }
}
