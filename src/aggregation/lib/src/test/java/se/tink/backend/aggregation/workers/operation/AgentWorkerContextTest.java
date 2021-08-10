package se.tink.backend.aggregation.workers.operation;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.concurrent.TimeUnit;
import org.apache.curator.framework.CuratorFramework;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.aggregationcontroller.ControllerWrapper;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateCredentialsStatusRequest;
import se.tink.backend.aggregation.api.AggregatorInfo;
import se.tink.backend.aggregation.controllers.ProviderSessionCacheController;
import se.tink.backend.aggregation.controllers.SupplementalInformationController;
import se.tink.backend.aggregation.events.AccountInformationServiceEventsProducer;
import se.tink.backend.aggregation.rpc.TransferRequest;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.credentials.service.RefreshInformationRequest;
import se.tink.libraries.credentials.service.UserAvailability;
import se.tink.libraries.metrics.collection.MetricCollector;
import se.tink.libraries.metrics.registry.MetricRegistry;
import se.tink.libraries.unleash.UnleashClient;

public class AgentWorkerContextTest {

    private MetricRegistry metricRegistry;
    private CuratorFramework curatorClient;
    private SupplementalInformationController supplementalInfoController;
    private AggregatorInfo aggregatorInfo;
    private ProviderSessionCacheController providerSessionCacheController;
    private ControllerWrapper controllerWrapper;
    private AccountInformationServiceEventsProducer accountInformationServiceEventsProducer;
    private UnleashClient unleashClient;
    private RequestStatusManager requestStatusManager;

    @Before
    public void setUp() {
        MetricCollector metricCollector = new MetricCollector();
        this.metricRegistry = new MetricRegistry(metricCollector);
        this.curatorClient = Mockito.mock(CuratorFramework.class, Mockito.RETURNS_DEEP_STUBS);
        this.aggregatorInfo = Mockito.mock(AggregatorInfo.class);
        this.supplementalInfoController = Mockito.mock(SupplementalInformationController.class);
        this.providerSessionCacheController = Mockito.mock(ProviderSessionCacheController.class);
        this.controllerWrapper = Mockito.mock(ControllerWrapper.class);
        this.accountInformationServiceEventsProducer =
                Mockito.mock(AccountInformationServiceEventsProducer.class);
        this.unleashClient = Mockito.mock(UnleashClient.class);
        this.requestStatusManager = Mockito.mock(RequestStatusManager.class);
    }

    private AgentWorkerContext buildAgentWorkerContext(CredentialsRequest request) {
        return new AgentWorkerContext(
                request,
                metricRegistry,
                curatorClient,
                aggregatorInfo,
                supplementalInfoController,
                providerSessionCacheController,
                controllerWrapper,
                "test",
                "two",
                "correlationId1234",
                accountInformationServiceEventsProducer,
                unleashClient,
                requestStatusManager);
    }

    @Test
    public void whenUpdatingCredentialsExcludingSensitiveRefreshIdIsPassedOnToControllerWrapper() {
        // given
        RefreshInformationRequest request = new RefreshInformationRequest();
        String testRefreshId = "testRefreshId";
        request.setRefreshId(testRefreshId);
        request.setProvider(new Provider());
        AgentWorkerContext context = buildAgentWorkerContext(request);
        Credentials credentials = new Credentials();

        // when
        context.updateCredentialsExcludingSensitiveInformation(credentials, false);

        // then
        verify(controllerWrapper, times(1))
                .updateCredentials(
                        argThat(
                                (UpdateCredentialsStatusRequest controllerRequest) ->
                                        controllerRequest.getRefreshId().equals(testRefreshId)));
    }

    @Test
    public void whenWaitingForSupplementalInformationTimesOutDoNotThrow() {
        RefreshInformationRequest request = new RefreshInformationRequest();
        String testRefreshId = "testRefreshId";
        request.setRefreshId(testRefreshId);
        request.setProvider(new Provider());
        Credentials credentials = new Credentials();
        request.setCredentials(credentials);
        UserAvailability userAvailability = new UserAvailability();
        userAvailability.setUserPresent(true);
        request.setUserAvailability(userAvailability);
        AgentWorkerContext context = buildAgentWorkerContext(request);

        context.waitForSupplementalInformation("testKey", 2, TimeUnit.SECONDS, "test");
    }

    @Test
    public void
            whenUpdatingCredentialsExcludingSensitiveWithNullRefreshIdNullRefreshIdIsPassedOnToControllerWrapper() {
        // given
        CredentialsRequest request = new TransferRequest();
        request.setProvider(new Provider());
        AgentWorkerContext context = buildAgentWorkerContext(request);
        Credentials credentials = new Credentials();

        // when
        context.updateCredentialsExcludingSensitiveInformation(credentials, false);

        // then
        verify(controllerWrapper, times(1))
                .updateCredentials(
                        argThat(
                                (UpdateCredentialsStatusRequest controllerRequest) ->
                                        controllerRequest.getRefreshId() == null));
    }
}
