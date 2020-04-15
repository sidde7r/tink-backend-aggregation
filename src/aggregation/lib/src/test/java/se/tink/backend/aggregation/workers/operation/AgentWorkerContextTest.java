package se.tink.backend.aggregation.workers.operation;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Locale;
import org.apache.curator.framework.CuratorFramework;
import org.junit.Assert;
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
import se.tink.backend.aggregation.rpc.TransferRequest;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.credentials.service.RefreshInformationRequest;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.i18n.LocalizableKey;
import se.tink.libraries.metrics.registry.MetricRegistry;

public class AgentWorkerContextTest {

    private MetricRegistry metricRegistry;
    private CuratorFramework curatorClient;
    private SupplementalInformationController supplementalInfoController;
    private AggregatorInfo aggregatorInfo;
    private ProviderSessionCacheController providerSessionCacheController;
    private ControllerWrapper controllerWrapper;

    @Before
    public void setUp() {
        this.metricRegistry = Mockito.mock(MetricRegistry.class);
        this.curatorClient = Mockito.mock(CuratorFramework.class);
        this.aggregatorInfo = Mockito.mock(AggregatorInfo.class);
        this.supplementalInfoController = Mockito.mock(SupplementalInformationController.class);
        this.providerSessionCacheController = Mockito.mock(ProviderSessionCacheController.class);
        this.controllerWrapper = Mockito.mock(ControllerWrapper.class);
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
                "two");
    }

    @Test
    public void testAggregationControllerRefreshId() {
        RefreshInformationRequest request = new RefreshInformationRequest();
        request.setRefreshId("TEST");
        request.setProvider(new Provider());
        AgentWorkerContext context = buildAgentWorkerContext(request);

        Credentials credentials = new Credentials();
        context.updateCredentialsExcludingSensitiveInformation(credentials, false, false);

        verify(controllerWrapper, times(1))
                .updateCredentials(
                        argThat(
                                (UpdateCredentialsStatusRequest controllerRequest) ->
                                        controllerRequest.getRefreshId().equals("TEST")));
    }

    @Test
    public void testRefreshWhenNotRefreshInformation() {
        CredentialsRequest request = new TransferRequest();
        request.setProvider(new Provider());
        AgentWorkerContext context = buildAgentWorkerContext(request);

        Credentials credentials = new Credentials();
        context.updateCredentialsExcludingSensitiveInformation(credentials, false, false);

        verify(controllerWrapper, times(1))
                .updateCredentials(
                        argThat(
                                (UpdateCredentialsStatusRequest controllerRequest) ->
                                        controllerRequest.getRefreshId() == null));
    }

    @Test
    public void testCotalagGetString() {
        String less_than_one_sek_en = "The transfer amount, less than 1 SEK is not supported.";
        String less_than_one_sek_sv = "Överföringsbelopp på mindre än 1 kr stöds inte.";
        Catalog catalog = new Catalog(new Locale("sv", "SE"));
        Assert.assertEquals(less_than_one_sek_sv, catalog.getString(less_than_one_sek_en));
        Assert.assertEquals(
                less_than_one_sek_sv, catalog.getString(new LocalizableKey(less_than_one_sek_en)));
    }
}
