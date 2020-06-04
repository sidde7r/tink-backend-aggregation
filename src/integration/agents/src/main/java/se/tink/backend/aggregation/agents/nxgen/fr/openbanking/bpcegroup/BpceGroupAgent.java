package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup;

import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.apiclient.BpceGroupApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.authenticator.BpceGroupAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.configuration.BpceGroupConfiguration;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.signature.BpceGroupRequestSigner;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.signature.BpceGroupSignatureHeaderGenerator;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.storage.BpceOAuth2TokenStorage;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.transactionalaccount.BpceGroupTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.transactionalaccount.BpceGroupTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.transactionalaccount.converter.BpceGroupTransactionalAccountConverter;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.libraries.credentials.service.CredentialsRequest;

public final class BpceGroupAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor, RefreshSavingsAccountsExecutor {

    private final BpceGroupApiClient bpceGroupApiClient;
    private final BpceOAuth2TokenStorage bpceOAuth2TokenStorage;
    private final StrongAuthenticationState strongAuthenticationState;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;

    public BpceGroupAgent(
            CredentialsRequest request,
            AgentContext context,
            AgentsServiceConfiguration agentsServiceConfiguration) {
        super(request, context, agentsServiceConfiguration.getSignatureKeyPair());

        final AgentConfiguration<BpceGroupConfiguration> agentConfiguration =
                getAgentConfiguration();
        final BpceGroupConfiguration bpceGroupConfiguration =
                agentConfiguration.getClientConfiguration();
        final BpceGroupSignatureHeaderGenerator bpceGroupSignatureHeaderGenerator =
                createSignatureHeaderGenerator(agentsServiceConfiguration, bpceGroupConfiguration);
        final String redirectUrl = agentConfiguration.getRedirectUrl();

        this.bpceOAuth2TokenStorage = new BpceOAuth2TokenStorage(this.persistentStorage);

        this.bpceGroupApiClient =
                new BpceGroupApiClient(
                        this.client,
                        this.bpceOAuth2TokenStorage,
                        bpceGroupConfiguration,
                        redirectUrl,
                        bpceGroupSignatureHeaderGenerator);

        this.strongAuthenticationState = new StrongAuthenticationState(request.getAppUriId());

        this.client.setEidasProxy(agentsServiceConfiguration.getEidasProxy());

        this.transactionalAccountRefreshController = getTransactionalAccountRefreshController();
    }

    @Override
    protected Authenticator constructAuthenticator() {
        final OAuth2AuthenticationController controller =
                new OAuth2AuthenticationController(
                        persistentStorage,
                        supplementalInformationHelper,
                        new BpceGroupAuthenticator(bpceGroupApiClient, bpceOAuth2TokenStorage),
                        credentials,
                        strongAuthenticationState);

        return new AutoAuthenticationController(
                request,
                systemUpdater,
                new ThirdPartyAppAuthenticationController<>(
                        controller, supplementalInformationHelper),
                controller);
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
    public SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }

    private BpceGroupSignatureHeaderGenerator createSignatureHeaderGenerator(
            AgentsServiceConfiguration agentsServiceConfiguration,
            BpceGroupConfiguration bpceGroupConfiguration) {

        final BpceGroupRequestSigner bpceGroupRequestSigner =
                new BpceGroupRequestSigner(
                        agentsServiceConfiguration.getEidasProxy(), getEidasIdentity());

        return new BpceGroupSignatureHeaderGenerator(
                bpceGroupConfiguration, bpceGroupRequestSigner);
    }

    private AgentConfiguration<BpceGroupConfiguration> getAgentConfiguration() {
        return getAgentConfigurationController()
                .getAgentCommonConfiguration(BpceGroupConfiguration.class);
    }

    private TransactionalAccountRefreshController getTransactionalAccountRefreshController() {
        final BpceGroupTransactionalAccountConverter bpceGroupTransactionalAccountConverter =
                new BpceGroupTransactionalAccountConverter();

        final BpceGroupTransactionalAccountFetcher accountFetcher =
                new BpceGroupTransactionalAccountFetcher(
                        bpceGroupApiClient, bpceGroupTransactionalAccountConverter);

        final BpceGroupTransactionFetcher transactionFetcher =
                new BpceGroupTransactionFetcher(bpceGroupApiClient);

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                accountFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionDatePaginationController<>(transactionFetcher)));
    }
}
