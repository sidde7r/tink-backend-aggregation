package se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;

import com.google.inject.Inject;
import java.net.URI;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.agentplatform.AgentPlatformHttpClient;
import se.tink.backend.aggregation.agents.agentplatform.authentication.AgentPlatformAuthenticator;
import se.tink.backend.aggregation.agents.agentplatform.authentication.ObjectMapperFactory;
import se.tink.backend.aggregation.agents.agentplatform.authentication.storage.AgentPlatformStorageMigration;
import se.tink.backend.aggregation.agents.agentplatform.authentication.storage.AgentPlatformStorageMigrator;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.authentication.KbcOauth2AuthenticationConfig;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.authentication.KbcStorageMigrator;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.configuration.KbcConfiguration;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.fetcher.KbcTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.BerlinGroupAccountFetcher;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcess;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationFlow;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;

@AgentCapabilities({CHECKING_ACCOUNTS})
public final class KbcAgent extends BerlinGroupAgent<KbcApiClient, KbcConfiguration>
        implements AgentPlatformAuthenticator, AgentPlatformStorageMigration {

    private ObjectMapperFactory objectMapperFactory;

    @Inject
    public KbcAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);

        this.apiClient = createApiClient();
        this.transactionalAccountRefreshController = getTransactionalAccountRefreshController();
        this.objectMapperFactory = new ObjectMapperFactory();
    }

    @Override
    protected KbcApiClient createApiClient() {
        return new KbcApiClient(
                client,
                getConfiguration().getProviderSpecificConfiguration(),
                request,
                getConfiguration().getRedirectUrl(),
                credentials,
                persistentStorage,
                getConfiguration().getQsealc());
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return OAuth2AuthenticationFlow.create(
                request,
                systemUpdater,
                persistentStorage,
                supplementalInformationHelper,
                new KbcAuthenticator(apiClient),
                credentials,
                strongAuthenticationState);
    }

    @Override
    protected Class<KbcConfiguration> getConfigurationClassDescription() {
        return KbcConfiguration.class;
    }

    @Override
    protected TransactionalAccountRefreshController getTransactionalAccountRefreshController() {
        final BerlinGroupAccountFetcher accountFetcher = new BerlinGroupAccountFetcher(apiClient);
        final KbcTransactionFetcher transactionFetcher = new KbcTransactionFetcher(apiClient);

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                accountFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionKeyPaginationController<>(transactionFetcher)));
    }

    @Override
    public AgentAuthenticationProcess getAuthenticationProcess() {
        final KbcConfiguration agentConfiguration =
                getConfiguration().getProviderSpecificConfiguration();
        final URI redirectUrl = URI.create(getConfiguration().getRedirectUrl());
        final AgentPlatformHttpClient httpClient = new AgentPlatformHttpClient(client);

        return new KbcOauth2AuthenticationConfig(
                        agentConfiguration,
                        redirectUrl,
                        httpClient,
                        objectMapperFactory.getInstance())
                .authenticationProcess();
    }

    @Override
    public boolean isBackgroundRefreshPossible() {
        return true;
    }

    @Override
    public AgentPlatformStorageMigrator getMigrator() {
        return new KbcStorageMigrator(objectMapperFactory.getInstance());
    }
}
