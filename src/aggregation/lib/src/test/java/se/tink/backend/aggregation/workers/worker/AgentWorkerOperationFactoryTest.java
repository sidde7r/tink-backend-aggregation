package se.tink.backend.aggregation.workers.worker;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.apache.curator.framework.CuratorFramework;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.aggregationcontroller.ControllerWrapper;
import se.tink.backend.aggregation.aggregationcontroller.v1.core.HostConfiguration;
import se.tink.backend.aggregation.cluster.identification.ClientInfo;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.controllers.ProviderSessionCacheController;
import se.tink.backend.aggregation.controllers.SupplementalInformationController;
import se.tink.backend.aggregation.events.CredentialsEventProducer;
import se.tink.backend.aggregation.events.DataTrackerEventProducer;
import se.tink.backend.aggregation.events.LoginAgentEventProducer;
import se.tink.backend.aggregation.storage.database.daos.CryptoConfigurationDao;
import se.tink.backend.aggregation.storage.database.providers.AggregatorInfoProvider;
import se.tink.backend.aggregation.storage.database.providers.ControllerWrapperProvider;
import se.tink.backend.aggregation.storage.debug.AgentDebugStorageHandler;
import se.tink.backend.aggregation.workers.commands.state.CircuitBreakerAgentWorkerCommandState;
import se.tink.backend.aggregation.workers.commands.state.DebugAgentWorkerCommandState;
import se.tink.backend.aggregation.workers.commands.state.InstantiateAgentWorkerCommandState;
import se.tink.backend.aggregation.workers.commands.state.LoginAgentWorkerCommandState;
import se.tink.backend.aggregation.workers.commands.state.ReportProviderMetricsAgentWorkerCommandState;
import se.tink.backend.aggregation.workers.concurrency.InterProcessSemaphoreMutexFactory;
import se.tink.backend.aggregation.workers.operation.AgentWorkerOperation;
import se.tink.backend.aggregation.workers.operation.AgentWorkerOperation.AgentWorkerOperationState;
import se.tink.backend.integration.agent_data_availability_tracker.client.AgentDataAvailabilityTrackerClient;
import se.tink.backend.integration.tpp_secrets_service.client.ManagedTppSecretsServiceClient;
import se.tink.libraries.cache.CacheClient;
import se.tink.libraries.credentials.service.CredentialsRequestType;
import se.tink.libraries.credentials.service.ManualAuthenticateRequest;
import se.tink.libraries.credentials.service.RefreshInformationRequest;
import se.tink.libraries.metrics.registry.MetricRegistry;

public final class AgentWorkerOperationFactoryTest {

    private static final String CLUSTER_ID = "my-cluster";
    private static final String PROVIDER_NAME = "myprovider";
    private static final String MARKET = "mymarket";

    private AgentWorkerOperationFactory factory;
    private ClientInfo clientInfo;
    private Provider provider;
    private CredentialsRequestType credentialsRequestType = CredentialsRequestType.CREATE;

    @Before
    public void setup() {
        // given
        ControllerWrapperProvider controllerWrapperProvider = mock(ControllerWrapperProvider.class);

        Injector injector = Guice.createInjector(new TestModule(controllerWrapperProvider));
        factory = injector.getInstance(AgentWorkerOperationFactory.class);

        provider = mock(Provider.class);
        when(provider.getName()).thenReturn(PROVIDER_NAME);
        when(provider.getMarket()).thenReturn(MARKET);

        clientInfo = mock(ClientInfo.class);
        when(clientInfo.getClusterId()).thenReturn(CLUSTER_ID);

        HostConfiguration hostConfiguration = mock(HostConfiguration.class);
        when(hostConfiguration.getClusterId()).thenReturn(CLUSTER_ID);

        ControllerWrapper controllerWrapper = mock(ControllerWrapper.class);
        when(controllerWrapper.getHostConfiguration()).thenReturn(hostConfiguration);
        when(controllerWrapperProvider.createControllerWrapper(CLUSTER_ID))
                .thenReturn(controllerWrapper);
    }

    @Test
    public void createdAuthenticateOperationShouldContainClusterIdFromClientInfo() {
        // given
        ManualAuthenticateRequest authenticateRequest = mock(ManualAuthenticateRequest.class);
        when(authenticateRequest.getProvider()).thenReturn(provider);
        when(authenticateRequest.getType()).thenReturn(credentialsRequestType);

        // when
        AgentWorkerOperation operation =
                factory.createOperationAuthenticate(authenticateRequest, clientInfo);

        // then
        assertThat(operation.getContext().getClusterId()).isEqualTo(CLUSTER_ID);
    }

    @Test
    public void createdRefreshOperationShouldContainClusterIdFromClientInfo() {
        // given
        RefreshInformationRequest refreshRequest = mock(RefreshInformationRequest.class);
        when(refreshRequest.getProvider()).thenReturn(provider);
        when(refreshRequest.getType()).thenReturn(credentialsRequestType);

        // when
        AgentWorkerOperation operation = factory.createOperationRefresh(refreshRequest, clientInfo);

        // then
        assertThat(operation.getContext().getClusterId()).isEqualTo(CLUSTER_ID);
    }

    private static class TestModule extends AbstractModule {

        private final ControllerWrapperProvider controllerWrapperProvider;

        public TestModule(ControllerWrapperProvider controllerWrapperProvider) {
            this.controllerWrapperProvider = controllerWrapperProvider;
        }

        @Override
        protected void configure() {
            // AgentWorkerOperationFactory
            bind(CacheClient.class).toInstance(mock(CacheClient.class));
            bind(MetricRegistry.class).toInstance(mock(MetricRegistry.class));
            bind(AgentDebugStorageHandler.class).toInstance(mock(AgentDebugStorageHandler.class));
            bind(AgentWorkerOperationState.class).toInstance(mock(AgentWorkerOperationState.class));
            bind(DebugAgentWorkerCommandState.class)
                    .toInstance(mock(DebugAgentWorkerCommandState.class));
            bind(CircuitBreakerAgentWorkerCommandState.class)
                    .toInstance(mock(CircuitBreakerAgentWorkerCommandState.class));
            bind(InstantiateAgentWorkerCommandState.class)
                    .toInstance(mock(InstantiateAgentWorkerCommandState.class));
            bind(LoginAgentWorkerCommandState.class)
                    .toInstance(mock(LoginAgentWorkerCommandState.class));
            bind(ReportProviderMetricsAgentWorkerCommandState.class)
                    .toInstance(mock(ReportProviderMetricsAgentWorkerCommandState.class));
            bind(SupplementalInformationController.class)
                    .toInstance(mock(SupplementalInformationController.class));
            bind(ProviderSessionCacheController.class)
                    .toInstance(mock(ProviderSessionCacheController.class));
            bind(CryptoConfigurationDao.class).toInstance(mock(CryptoConfigurationDao.class));
            bind(ControllerWrapperProvider.class).toInstance(controllerWrapperProvider);
            bind(AggregatorInfoProvider.class).toInstance(mock(AggregatorInfoProvider.class));
            bind(CuratorFramework.class).toInstance(mock(CuratorFramework.class));
            bind(AgentsServiceConfiguration.class)
                    .toInstance(mock(AgentsServiceConfiguration.class));
            bind(CredentialsEventProducer.class).toInstance(mock(CredentialsEventProducer.class));
            bind(DataTrackerEventProducer.class).toInstance(mock(DataTrackerEventProducer.class));
            bind(LoginAgentEventProducer.class).toInstance(mock(LoginAgentEventProducer.class));
            bind(AgentDataAvailabilityTrackerClient.class)
                    .toInstance(mock(AgentDataAvailabilityTrackerClient.class));
            bind(ManagedTppSecretsServiceClient.class)
                    .toInstance(mock(ManagedTppSecretsServiceClient.class));
            bind(InterProcessSemaphoreMutexFactory.class)
                    .toInstance(mock(InterProcessSemaphoreMutexFactory.class));
        }
    }
}
