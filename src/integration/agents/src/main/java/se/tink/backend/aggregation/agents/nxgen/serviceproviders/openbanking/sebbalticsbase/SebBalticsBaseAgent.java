package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbalticsbase;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbalticsbase.authenticator.SebBalticsDecoupledAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbalticsbase.configuration.SebBalticsConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbalticsbase.fetcher.transactionalaccount.SebBalticsTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbalticsbase.fetcher.transactionalaccount.SebBalticsTransactionalAccountFetcher;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.nxgen.agents.SubsequentProgressiveGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.StatelessProgressiveAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.OAuth2TokenSessionHandler;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

public abstract class SebBalticsBaseAgent<C extends SebBalticsBaseApiClient>
        extends SubsequentProgressiveGenerationAgent {

    protected C apiClient;
    protected AgentConfiguration<SebBalticsConfiguration> agentConfiguration;
    protected SebBalticsConfiguration sebConfiguration;

    protected SebBalticsBaseAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);
        this.apiClient = getApiClient();
    }

    protected abstract C getApiClient();

    @Override
    public void setConfiguration(final AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);

        agentConfiguration =
                getAgentConfigurationController()
                        .getAgentConfiguration(SebBalticsConfiguration.class);
        sebConfiguration = agentConfiguration.getProviderSpecificConfiguration();
        apiClient.setConfiguration(sebConfiguration);
        client.setEidasProxy(configuration.getEidasProxy());
    }

    @Override
    public StatelessProgressiveAuthenticator getAuthenticator() {
        return new SebBalticsDecoupledAuthenticator(
                apiClient, agentConfiguration, sessionStorage, persistentStorage, credentials);
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new OAuth2TokenSessionHandler(persistentStorage);
    }

    protected TransactionalAccountRefreshController getTransactionalAccountRefreshController() {
        SebBalticsTransactionalAccountFetcher accountFetcher =
                new SebBalticsTransactionalAccountFetcher(apiClient);

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                accountFetcher,
                new TransactionFetcherController<>(
                        this.transactionPaginationHelper,
                        new TransactionKeyPaginationController<>(
                                new SebBalticsTransactionFetcher(
                                        apiClient, transactionPaginationHelper))));
    }
}
