package se.tink.backend.aggregation.workers.worker;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.base.Predicate;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;
import java.net.URI;
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
import se.tink.backend.aggregation.events.AccountInformationServiceEventsProducer;
import se.tink.backend.aggregation.events.CredentialsEventProducer;
import se.tink.backend.aggregation.events.DataTrackerEventProducer;
import se.tink.backend.aggregation.events.LoginAgentEventProducer;
import se.tink.backend.aggregation.events.RefreshEventProducer;
import se.tink.backend.aggregation.rpc.ConfigureWhitelistInformationRequest;
import se.tink.backend.aggregation.rpc.RefreshWhitelistInformationRequest;
import se.tink.backend.aggregation.rpc.TransferRequest;
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
import se.tink.backend.aggregation.workers.worker.conditions.annotation.ShouldAddExtraCommands;
import se.tink.backend.integration.agent_data_availability_tracker.client.AsAgentDataAvailabilityTrackerClient;
import se.tink.backend.integration.tpp_secrets_service.client.ManagedTppSecretsServiceClient;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.cache.CacheClient;
import se.tink.libraries.credentials.service.CredentialsRequestType;
import se.tink.libraries.credentials.service.ManualAuthenticateRequest;
import se.tink.libraries.credentials.service.RefreshInformationRequest;
import se.tink.libraries.enums.MarketCode;
import se.tink.libraries.metrics.registry.MetricRegistry;
import se.tink.libraries.provider.ProviderDto;
import se.tink.libraries.transfer.rpc.Transfer;

public final class AgentWorkerOperationFactoryTest {

    private static final String CLUSTER_ID = "my-cluster";
    private static final String PROVIDER_NAME = "myprovider";
    private static final String MARKET = "mymarket";
    private static final String APP_ID = "mockedAppId";
    private static final String CORRELATION_ID = "correlation-id";

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

    @Test
    public void createdRefreshOperationShouldContainCorrelationIdFromRequest() {
        // given
        RefreshInformationRequest refreshRequest = mock(RefreshInformationRequest.class);
        when(refreshRequest.getProvider()).thenReturn(provider);
        when(refreshRequest.getType()).thenReturn(credentialsRequestType);
        when(refreshRequest.getRefreshId()).thenReturn(CORRELATION_ID);

        // when
        AgentWorkerOperation operation = factory.createOperationRefresh(refreshRequest, clientInfo);

        // then
        assertThat(operation.getContext().getRefreshId()).hasValue(CORRELATION_ID);
    }

    @Test
    public void createdConfigureWhiteListShouldContainCorrelationIdFromRequest() {
        // given
        ConfigureWhitelistInformationRequest refreshRequest =
                mock(ConfigureWhitelistInformationRequest.class);
        when(refreshRequest.getProvider()).thenReturn(provider);
        when(refreshRequest.getType()).thenReturn(credentialsRequestType);
        when(refreshRequest.getRefreshId()).thenReturn(CORRELATION_ID);

        // when
        AgentWorkerOperation operation =
                factory.createOperationConfigureWhitelist(refreshRequest, clientInfo);

        // then
        assertThat(operation.getContext().getRefreshId()).hasValue(CORRELATION_ID);
    }

    @Test
    public void commandsForRefreshAreRunWhenSkipRefreshFlagIsFalse() {
        // Arrange
        TransferRequest request = mock(TransferRequest.class);
        AgentWorkerOperationFactory factorySpy = spy(this.factory);
        when(clientInfo.getAppId()).thenReturn(APP_ID);
        when(request.getProvider()).thenReturn(provider);
        when(request.getType()).thenReturn(CredentialsRequestType.TRANSFER);

        // Act
        factorySpy.createOperationExecuteTransfer(request, clientInfo);

        // Assert
        verify(factorySpy).createRefreshAccountsCommands(any(), any(), any());
    }

    @Test
    public void commandsForRefreshAreSkippedWhenSkipRefreshFlagIsTrue() {
        // Arrange
        TransferRequest request = mock(TransferRequest.class);
        AgentWorkerOperationFactory factorySpy = spy(this.factory);
        when(clientInfo.getAppId()).thenReturn(APP_ID);
        when(request.getProvider()).thenReturn(provider);
        when(request.isSkipRefresh()).thenReturn(true);
        when(request.getType()).thenReturn(CredentialsRequestType.TRANSFER);

        // Act
        factorySpy.createOperationExecuteTransfer(request, clientInfo);

        // Assert
        verify(factorySpy, times(0)).createRefreshAccountsCommands(any(), any(), any());
    }

    @Test
    public void commandsWithoutLoginStepAreRunWhenProviderIsUKOB() {
        // Arrange
        TransferRequest request = mock(TransferRequest.class);
        AgentWorkerOperationFactory factorySpy = spy(this.factory);
        when(clientInfo.getAppId()).thenReturn(APP_ID);
        when(request.getProvider()).thenReturn(provider);
        when(request.getType()).thenReturn(CredentialsRequestType.TRANSFER);
        when(provider.getMarket()).thenReturn(MarketCode.GB.toString());
        when(provider.isOpenBanking()).thenReturn(true);

        // Act
        factorySpy.createOperationExecuteTransfer(request, clientInfo);

        // Assert
        verify(factorySpy)
                .createTransferWithoutRefreshBaseCommands(any(), any(), any(), any(), any());
    }

    @Test
    public void commandsWithoutLoginStepAreRunWhenProviderIsFrenchTestProvider() {
        // Arrange
        TransferRequest request = mock(TransferRequest.class);
        AgentWorkerOperationFactory factorySpy = spy(this.factory);
        when(clientInfo.getAppId()).thenReturn(APP_ID);
        when(request.getProvider()).thenReturn(provider);
        when(request.getType()).thenReturn(CredentialsRequestType.TRANSFER);
        when(provider.getMarket()).thenReturn(MarketCode.FR.toString());
        when(provider.getType()).thenReturn(ProviderDto.ProviderTypes.TEST);

        // Act
        factorySpy.createOperationExecuteTransfer(request, clientInfo);

        // Assert
        verify(factorySpy)
                .createTransferWithoutRefreshBaseCommands(any(), any(), any(), any(), any());
    }

    @Test
    public void createdWhitelistRefreshShouldContainCorrelationIdFromRequest() {
        // given
        RefreshWhitelistInformationRequest refreshRequest =
                mock(RefreshWhitelistInformationRequest.class);
        when(refreshRequest.getProvider()).thenReturn(provider);
        when(refreshRequest.getType()).thenReturn(credentialsRequestType);
        when(refreshRequest.getRefreshId()).thenReturn(CORRELATION_ID);

        // when
        AgentWorkerOperation operation =
                factory.createOperationWhitelistRefresh(refreshRequest, clientInfo);

        // then
        assertThat(operation.getContext().getRefreshId()).hasValue(CORRELATION_ID);
    }

    @Test
    public void testAisPlusPisFlowLogic() {
        TransferRequest request = mock(TransferRequest.class);
        Provider pisProvider = provider = mock(Provider.class);
        Transfer payment = mock(Transfer.class);
        when(request.getTransfer()).thenReturn(payment);
        // SE case, regardless of source account
        when(request.getProvider()).thenReturn(pisProvider);
        when(pisProvider.getName()).thenReturn("danskebank-bankid");
        assertThat(factory.isAisPlusPisFlow(request)).isTrue();
        // UK case, classical
        when(payment.getSource()).thenReturn(null);
        when(pisProvider.getName()).thenReturn("uk-revolut-oauth2");
        assertThat(factory.isAisPlusPisFlow(request)).isFalse();
        // UK case, with source account
        when(payment.getSource())
                .thenReturn(AccountIdentifier.create(URI.create("sort-code://12345612345678")));
        when(pisProvider.getName()).thenReturn("uk-barclays-oauth2");
        assertThat(factory.isAisPlusPisFlow(request)).isFalse();
        // FR case, with source account
        when(payment.getSource())
                .thenReturn(
                        AccountIdentifier.create(URI.create("iban://FR1420041010050500013M02606")));
        when(pisProvider.getName()).thenReturn("fr-bnpparibas-ob");
        assertThat(factory.isAisPlusPisFlow(request)).isFalse();
        // IT case, regardless of source account
        when(pisProvider.getName()).thenReturn("it-bancasella-ob");
        assertThat(factory.isAisPlusPisFlow(request)).isTrue();
        // Revolut SE
        when(pisProvider.getName()).thenReturn("se-revolut-ob");
        assertThat(factory.isAisPlusPisFlow(request)).isFalse();
    }

    private static class TestModule extends AbstractModule {

        private final ControllerWrapperProvider controllerWrapperProvider;

        TestModule(ControllerWrapperProvider controllerWrapperProvider) {
            this.controllerWrapperProvider = controllerWrapperProvider;
        }

        @Override
        protected void configure() {
            // AgentWorkerOperationFactory
            bind(CacheClient.class).toInstance(mock(CacheClient.class));
            bind(MetricRegistry.class).toInstance(mock(MetricRegistry.class));
            bind(AgentDebugStorageHandler.class).toInstance(mock(AgentDebugStorageHandler.class));
            bind(new TypeLiteral<Predicate<Provider>>() {})
                    .annotatedWith(ShouldAddExtraCommands.class)
                    .toInstance(p -> false);
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
            bind(RefreshEventProducer.class).toInstance(mock(RefreshEventProducer.class));
            bind(AsAgentDataAvailabilityTrackerClient.class)
                    .toInstance(mock(AsAgentDataAvailabilityTrackerClient.class));
            bind(ManagedTppSecretsServiceClient.class)
                    .toInstance(mock(ManagedTppSecretsServiceClient.class));
            bind(InterProcessSemaphoreMutexFactory.class)
                    .toInstance(mock(InterProcessSemaphoreMutexFactory.class));
            bind(AccountInformationServiceEventsProducer.class)
                    .toInstance(mock(AccountInformationServiceEventsProducer.class));
        }
    }
}
