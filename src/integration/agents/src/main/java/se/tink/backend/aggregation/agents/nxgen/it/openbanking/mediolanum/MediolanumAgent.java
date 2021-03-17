package se.tink.backend.aggregation.agents.nxgen.it.openbanking.mediolanum;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.mediolanum.authenticator.MediolanumAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.mediolanum.authenticator.MediolanumRedirectHelper;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.mediolanum.fetcher.AccountMapper;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.mediolanum.fetcher.MediolanumAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.mediolanum.fetcher.MediolanumTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.mediolanum.fetcher.TransactionMapper;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

@AgentCapabilities({CHECKING_ACCOUNTS})
public final class MediolanumAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor, RefreshSavingsAccountsExecutor {

    private final MediolanumConfiguration mediolanumConfiguration;
    private final MediolanumStorage mediolanumStorage;
    private final MediolanumApiClient apiClient;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;

    @Inject
    public MediolanumAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);
        this.mediolanumStorage = new MediolanumStorage(persistentStorage);
        this.mediolanumConfiguration = constructConfiguration();
        this.apiClient = constructApiClient(componentProvider);
        this.transactionalAccountRefreshController =
                constructTransactionalAccountRefreshController(componentProvider);
    }

    private MediolanumConfiguration constructConfiguration() {
        AgentConfiguration<MediolanumConfiguration> agentConfiguration =
                getAgentConfigurationController()
                        .getAgentConfiguration(MediolanumConfiguration.class);
        MediolanumConfiguration providerSpecificConfiguration =
                agentConfiguration.getProviderSpecificConfiguration();
        providerSpecificConfiguration.setRedirectUrl(agentConfiguration.getRedirectUrl());
        providerSpecificConfiguration.setUserIp(request.isManual() ? userIp : null);
        return providerSpecificConfiguration;
    }

    @Override
    public void setConfiguration(AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);
        client.setEidasProxy(configuration.getEidasProxy());
        apiClient.setEidasProxy(configuration.getEidasProxy());
    }

    protected MediolanumApiClient constructApiClient(AgentComponentProvider componentProvider) {
        return new MediolanumApiClient(
                componentProvider.getTinkHttpClient(),
                componentProvider.getRandomValueGenerator(),
                componentProvider.getLocalDateTimeSource(),
                mediolanumConfiguration,
                mediolanumStorage);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        MediolanumRedirectHelper helper =
                new MediolanumRedirectHelper(mediolanumStorage, apiClient, mediolanumConfiguration);

        MediolanumAuthenticator authenticator =
                new MediolanumAuthenticator(
                        new OAuth2AuthenticationController(
                                persistentStorage,
                                supplementalInformationHelper,
                                helper,
                                credentials,
                                strongAuthenticationState),
                        apiClient,
                        mediolanumStorage,
                        credentials,
                        strongAuthenticationState,
                        supplementalInformationHelper);

        return new AutoAuthenticationController(
                request,
                context,
                new ThirdPartyAppAuthenticationController<String>(
                        authenticator, supplementalInformationHelper),
                authenticator);
    }

    protected TransactionalAccountRefreshController constructTransactionalAccountRefreshController(
            AgentComponentProvider componentProvider) {
        MediolanumAccountFetcher accountFetcher =
                new MediolanumAccountFetcher(apiClient, new AccountMapper());
        MediolanumTransactionFetcher transactionFetcher =
                new MediolanumTransactionFetcher(
                        apiClient,
                        new TransactionMapper(),
                        componentProvider.getLocalDateTimeSource(),
                        request.isManual());
        return new TransactionalAccountRefreshController(
                metricRefreshController, updateController, accountFetcher, transactionFetcher);
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
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }
}
