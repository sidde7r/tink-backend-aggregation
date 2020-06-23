package se.tink.backend.aggregation.agents.nxgen.no.openbanking.norwegian;

import java.util.Objects;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.norwegian.authenticator.NorwegianAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.norwegian.client.NorwegianApiClient;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.norwegian.client.NorwegianSigningFilter;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.norwegian.fetcher.transactionalaccount.NorwegianTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.norwegian.fetcher.transactionalaccount.NorwegianTransactionalAccountFetcher;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.eidassigner.QsealcAlg;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;
import se.tink.backend.aggregation.eidassigner.QsealcSignerImpl;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class NorwegianAgent extends NextGenerationAgent implements RefreshCheckingAccountsExecutor {

    private final NorwegianApiClient apiClient;
    private AgentConfiguration<NorwegianConfiguration> agentConfiguration;
    private NorwegianConfiguration norwegianConfiguration;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;

    public NorwegianAgent(
            CredentialsRequest request,
            AgentContext context,
            AgentsServiceConfiguration agentsServiceConfiguration) {
        super(request, context, agentsServiceConfiguration.getSignatureKeyPair());
        Objects.requireNonNull(request);
        Objects.requireNonNull(context);
        Objects.requireNonNull(agentsServiceConfiguration);
        this.agentConfiguration =
                getAgentConfigurationController()
                        .getAgentCommonConfiguration(NorwegianConfiguration.class);
        this.norwegianConfiguration = agentConfiguration.getClientConfiguration();
        this.apiClient =
                new NorwegianApiClient(
                        client, sessionStorage, persistentStorage, agentConfiguration);
        transactionalAccountRefreshController = getTransactionalAccountRefreshController();
        client.setEidasProxy(agentsServiceConfiguration.getEidasProxy());
        client.addFilter(
                new NorwegianSigningFilter(
                        norwegianConfiguration.getKeyId(),
                        getQsealcSigner(agentsServiceConfiguration)));
    }

    @Override
    protected Authenticator constructAuthenticator() {
        final OAuth2AuthenticationController controller =
                new OAuth2AuthenticationController(
                        persistentStorage,
                        supplementalInformationHelper,
                        new NorwegianAuthenticator(
                                apiClient, sessionStorage, norwegianConfiguration),
                        credentials,
                        strongAuthenticationState);

        return new AutoAuthenticationController(
                request,
                systemUpdater,
                new ThirdPartyAppAuthenticationController<>(
                        controller, supplementalInformationHelper),
                controller);
    }

    private QsealcSigner getQsealcSigner(AgentsServiceConfiguration agentsServiceConfiguration) {
        return QsealcSignerImpl.build(
                agentsServiceConfiguration.getEidasProxy().toInternalConfig(),
                QsealcAlg.EIDAS_RSA_SHA256,
                getEidasIdentity());
    }

    private TransactionalAccountRefreshController getTransactionalAccountRefreshController() {
        NorwegianTransactionalAccountFetcher accountFetcher =
                new NorwegianTransactionalAccountFetcher(apiClient);
        NorwegianTransactionFetcher transactionFetcher =
                new NorwegianTransactionFetcher(apiClient, persistentStorage, sessionStorage);

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                accountFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionDatePaginationController<>(transactionFetcher)));
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
}
