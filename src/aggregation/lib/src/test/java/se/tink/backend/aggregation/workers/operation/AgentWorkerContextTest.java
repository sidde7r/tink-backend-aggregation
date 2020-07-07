package se.tink.backend.aggregation.workers.operation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import org.apache.curator.framework.CuratorFramework;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.AccountHolder;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.aggregationcontroller.ControllerWrapper;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateCredentialsStatusRequest;
import se.tink.backend.aggregation.api.AggregatorInfo;
import se.tink.backend.aggregation.compliance.regulatory_restrictions.RegulatoryRestrictions;
import se.tink.backend.aggregation.controllers.ProviderSessionCacheController;
import se.tink.backend.aggregation.controllers.SupplementalInformationController;
import se.tink.backend.aggregation.rpc.TransferRequest;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.credentials.service.RefreshInformationRequest;
import se.tink.libraries.metrics.collection.MetricCollector;
import se.tink.libraries.metrics.registry.MetricRegistry;
import se.tink.libraries.pair.Pair;

public class AgentWorkerContextTest {

    private MetricRegistry metricRegistry;
    private CuratorFramework curatorClient;
    private SupplementalInformationController supplementalInfoController;
    private AggregatorInfo aggregatorInfo;
    private ProviderSessionCacheController providerSessionCacheController;
    private ControllerWrapper controllerWrapper;
    private RegulatoryRestrictions regulatoryRestrictions;

    @Before
    public void setUp() {
        MetricCollector metricCollector = new MetricCollector();
        this.metricRegistry = new MetricRegistry(metricCollector);
        this.curatorClient = Mockito.mock(CuratorFramework.class, Mockito.RETURNS_DEEP_STUBS);
        this.aggregatorInfo = Mockito.mock(AggregatorInfo.class);
        this.supplementalInfoController = Mockito.mock(SupplementalInformationController.class);
        this.providerSessionCacheController = Mockito.mock(ProviderSessionCacheController.class);
        this.controllerWrapper = Mockito.mock(ControllerWrapper.class);
        this.regulatoryRestrictions = Mockito.mock(RegulatoryRestrictions.class);
        Mockito.when(regulatoryRestrictions.shouldAccountBeRestricted(any(), any(), any()))
                .thenReturn(true);
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
                regulatoryRestrictions);
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
        context.updateCredentialsExcludingSensitiveInformation(credentials, false, false);

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
        AgentWorkerContext context = buildAgentWorkerContext(request);

        context.waitForSupplementalInformation("testKey", 2, TimeUnit.SECONDS);
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
        context.updateCredentialsExcludingSensitiveInformation(credentials, false, false);

        // then
        verify(controllerWrapper, times(1))
                .updateCredentials(
                        argThat(
                                (UpdateCredentialsStatusRequest controllerRequest) ->
                                        controllerRequest.getRefreshId() == null));
    }

    @Test
    public void
            whenUpdatingAccountHolderForAccountWithNullAccountHolderControllerWrapperNotCalled() {
        // given
        String tinkId = "id";
        String uniqueId = "uniqueId";
        Account account = Mockito.mock(Account.class);
        Mockito.when(account.getBankId()).thenReturn(uniqueId);

        AgentWorkerContext context =
                buildAgentWorkerContext(Mockito.mock(RefreshInformationRequest.class));
        context.updatedAccountsByTinkId.put(tinkId, account);
        context.allAvailableAccountsByUniqueId.put(uniqueId, Pair.of(account, null));

        // when
        AccountHolder accountHolder = context.sendAccountHolderToUpdateService(tinkId);

        // then
        Assert.assertNull(accountHolder);
        verify(controllerWrapper, times(0)).updateAccountHolder(any());
    }

    @Test
    public void whenUpdatingAccountHolderControllerWrapperCalledWithSameObject() {
        // given
        String tinkId = "id";
        String uniqueId = "uniqueId";
        String userId = "userId";

        Credentials credentials = new Credentials();
        credentials.setUserId(userId);

        AccountHolder holder = new AccountHolder();
        Account account = Mockito.mock(Account.class);
        Mockito.when(account.getBankId()).thenReturn(uniqueId);
        Mockito.when(account.getAccountHolder()).thenReturn(holder);

        RefreshInformationRequest credentialsRequest =
                Mockito.mock(RefreshInformationRequest.class);
        Mockito.when(credentialsRequest.getCredentials()).thenReturn(credentials);

        AgentWorkerContext context = buildAgentWorkerContext(credentialsRequest);
        context.updatedAccountsByTinkId.put(tinkId, account);
        context.allAvailableAccountsByUniqueId.put(uniqueId, Pair.of(account, null));

        // when
        AccountHolder accountHolder = context.sendAccountHolderToUpdateService(tinkId);

        // then
        Assert.assertNull(accountHolder);
        verify(controllerWrapper, times(1))
                .updateAccountHolder(
                        Mockito.argThat(
                                request -> Objects.equals(request.getAccountHolder(), holder)));
    }
}
