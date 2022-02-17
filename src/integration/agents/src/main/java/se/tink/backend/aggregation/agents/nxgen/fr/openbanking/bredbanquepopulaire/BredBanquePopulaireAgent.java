package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire;

import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.IDENTITY_DATA;

import com.google.inject.Inject;
import se.tink.agent.sdk.utils.signer.qsealc.QsealcAlgorithm;
import se.tink.agent.sdk.utils.signer.qsealc.QsealcSigner;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshIdentityDataExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.apiclient.BredBanquePopulaireApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.apiclient.BredBanquePopulaireResponseHandler;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.authenticator.BredBanquePopulaireAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.configuration.BredBanquePopulaireConfiguration;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.fetcher.identity.BredBanquePopulaireIdentityDataFetcher;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.fetcher.transacitons.BredBanquePopulaireTransactionsFetcher;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.fetcher.transactionalaccount.BredBanquePopulaireTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.signature.BredBanquePopulaireHeaderGenerator;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.client.LoggingStrategy;

@AgentCapabilities({CHECKING_ACCOUNTS, IDENTITY_DATA})
public final class BredBanquePopulaireAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor, RefreshIdentityDataExecutor {
    private final AgentConfiguration<BredBanquePopulaireConfiguration> agentConfiguration;
    private final QsealcSigner qsealcSigner;
    private final BredBanquePopulaireApiClient apiClient;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private final BredBanquePopulaireIdentityDataFetcher identityDataFetcher;

    @Inject
    public BredBanquePopulaireAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);

        setJsonHttpTrafficLogsEnabled(true);
        client.setLoggingStrategy(LoggingStrategy.EXPERIMENTAL);

        this.agentConfiguration = getAgentConfiguration();
        this.qsealcSigner = componentProvider.getQsealcSigner();
        final BredBanquePopulaireHeaderGenerator headerGenerator =
                new BredBanquePopulaireHeaderGenerator(
                        qsealcSigner,
                        QsealcAlgorithm.RSA_SHA256,
                        sessionStorage,
                        agentConfiguration.getProviderSpecificConfiguration().getKeyId());
        this.client.setResponseStatusHandler(new BredBanquePopulaireResponseHandler());
        this.apiClient =
                new BredBanquePopulaireApiClient(
                        client,
                        persistentStorage,
                        agentConfiguration,
                        componentProvider.getRandomValueGenerator(),
                        headerGenerator);
        this.transactionalAccountRefreshController = getTransactionalAccountRefreshController();
        this.identityDataFetcher = new BredBanquePopulaireIdentityDataFetcher(apiClient);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        final OAuth2AuthenticationController oAuth2Controller =
                new OAuth2AuthenticationController(
                        persistentStorage,
                        supplementalInformationHelper,
                        new BredBanquePopulaireAuthenticator(apiClient, persistentStorage),
                        credentials,
                        strongAuthenticationState);

        return new AutoAuthenticationController(
                request,
                systemUpdater,
                new ThirdPartyAppAuthenticationController<>(
                        oAuth2Controller, supplementalInformationHelper),
                oAuth2Controller);
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }

    @Override
    public void setConfiguration(AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);
        client.setEidasProxy(configuration.getEidasProxy());
    }

    private AgentConfiguration<BredBanquePopulaireConfiguration> getAgentConfiguration() {
        return getAgentConfigurationController()
                .getAgentConfiguration(BredBanquePopulaireConfiguration.class);
    }

    private TransactionalAccountRefreshController getTransactionalAccountRefreshController() {
        final BredBanquePopulaireTransactionalAccountFetcher accountFetcher =
                new BredBanquePopulaireTransactionalAccountFetcher(apiClient);

        final BredBanquePopulaireTransactionsFetcher<TransactionalAccount> transactionsFetcher =
                new BredBanquePopulaireTransactionsFetcher<>(apiClient);

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                accountFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionPagePaginationController<>(transactionsFetcher, 0)));
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
    public FetchIdentityDataResponse fetchIdentityData() {
        return new FetchIdentityDataResponse(identityDataFetcher.fetchIdentityData());
    }
}
