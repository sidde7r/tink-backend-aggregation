package se.tink.backend.aggregation.workers.commands.login;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.cluster.identification.ClientInfo;
import se.tink.backend.aggregation.workers.commands.metrics.MetricsCommand;
import se.tink.backend.aggregation.workers.metrics.AgentWorkerCommandMetricState;
import se.tink.backend.aggregation.workers.metrics.MetricAction;
import se.tink.backend.aggregation.workers.metrics.MetricActionComposite;
import se.tink.backend.aggregationcontroller.v1.rpc.enums.CredentialsRequestType;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.credentials.service.UserAvailability;
import se.tink.libraries.metrics.registry.MetricRegistry;
import src.libraries.interaction_counter.InteractionCounter;

@RunWith(JUnitParamsRunner.class)
public class MetricsFactoryTest {

    private MetricsFactory metricsFactory;
    private CredentialsRequest credentialsRequest;
    private InteractionCounter interactionCounter;

    @Before
    public void init() {
        this.interactionCounter = mock(InteractionCounter.class);
        prepareCredentialsRequestMock();

        AgentWorkerCommandMetricState commandMetricState = prepareCommandMetricsState();

        MetricsCommand metricsCommand = mock(MetricsCommand.class);
        when(metricsCommand.getMetricName()).thenReturn("metricName");
        commandMetricState.init(metricsCommand);

        this.metricsFactory = new MetricsFactory(commandMetricState);
    }

    private void prepareCredentialsRequestMock() {
        this.credentialsRequest = mock(CredentialsRequest.class);
        Provider provider = mock(Provider.class);
        when(provider.getMetricTypeName()).thenReturn("metricTypeName");
        when(provider.getMarket()).thenReturn("market");
        when(provider.getClassName()).thenReturn("className");
        when(credentialsRequest.getProvider()).thenReturn(provider);
        Credentials credentials = mock(Credentials.class);
        when(credentials.getMetricTypeName()).thenReturn("credentialsMetricTypeName");
        when(credentialsRequest.getCredentials()).thenReturn(credentials);
    }

    private AgentWorkerCommandMetricState prepareCommandMetricsState() {
        MetricRegistry metricRegistry = mock(MetricRegistry.class);

        ClientInfo clientInfo = mock(ClientInfo.class);
        when(clientInfo.getClusterId()).thenReturn("clusterId");

        return new AgentWorkerCommandMetricState(
                credentialsRequest, metricRegistry, CredentialsRequestType.MIGRATE, clientInfo);
    }

    @Test
    public void shouldReturnBackgroundRefreshWhenThereIsNoInteractionAndUserIsNotPresent() {
        // given
        UserAvailability userAvailability = new UserAvailability();
        userAvailability.setUserPresent(false);

        when(interactionCounter.getNumberInteractions()).thenReturn(0);
        when(credentialsRequest.isCreate()).thenReturn(false);
        when(credentialsRequest.isUpdate()).thenReturn(false);
        when(credentialsRequest.isForceAuthenticate()).thenReturn(false);
        when(credentialsRequest.getUserAvailability()).thenReturn(userAvailability);

        // when
        MetricActionComposite loginMetric =
                (MetricActionComposite)
                        metricsFactory.createLoginMetric(credentialsRequest, interactionCounter);
        MetricAction specificLoginMetric = (MetricAction) loginMetric.getMetricActions().get(1);

        // then
        assertThat(specificLoginMetric.getActionName()).isEqualTo("login-cron");
    }

    @Test
    public void shouldReturnAutoRefreshWhenThereIsNoSupplementalInteractionAndUserIsPresent() {
        // given
        UserAvailability userAvailability = new UserAvailability();
        userAvailability.setUserPresent(true);

        when(interactionCounter.getNumberInteractions()).thenReturn(0);
        when(credentialsRequest.isCreate()).thenReturn(false);
        when(credentialsRequest.isUpdate()).thenReturn(false);
        when(credentialsRequest.isForceAuthenticate()).thenReturn(false);
        when(credentialsRequest.getUserAvailability()).thenReturn(userAvailability);

        // when
        MetricActionComposite loginMetric =
                (MetricActionComposite)
                        metricsFactory.createLoginMetric(credentialsRequest, interactionCounter);
        MetricAction specificLoginMetric = (MetricAction) loginMetric.getMetricActions().get(1);

        // then
        assertThat(specificLoginMetric.getActionName()).isEqualTo("login-auto");
    }

    @Test
    public void shouldReturnLoginManualWhenThereIsASupplementalInteraction() {
        // given
        UserAvailability userAvailability = new UserAvailability();
        userAvailability.setUserPresent(true);

        when(interactionCounter.getNumberInteractions()).thenReturn(1);
        when(credentialsRequest.isCreate()).thenReturn(false);
        when(credentialsRequest.isUpdate()).thenReturn(false);
        when(credentialsRequest.isForceAuthenticate()).thenReturn(false);
        when(credentialsRequest.getUserAvailability()).thenReturn(userAvailability);

        // when
        MetricActionComposite loginMetric =
                (MetricActionComposite)
                        metricsFactory.createLoginMetric(credentialsRequest, interactionCounter);
        MetricAction specificLoginMetric = (MetricAction) loginMetric.getMetricActions().get(1);

        // then
        assertThat(specificLoginMetric.getActionName()).isEqualTo("login-manual");
    }

    @Test
    @Parameters({"true, false, false", "false, true, false", "false, false, true"})
    public void
            shouldReturnLoginManualWhenThereIsNoSupplementalInteractionButRequestRequiringAuthentication(
                    Boolean isCreate, Boolean isUpdate, Boolean isForceAuthentication) {
        // given
        UserAvailability userAvailability = new UserAvailability();
        userAvailability.setUserAvailableForInteraction(true);
        userAvailability.setUserPresent(true);

        when(interactionCounter.getNumberInteractions()).thenReturn(0);
        when(credentialsRequest.isCreate()).thenReturn(isCreate);
        when(credentialsRequest.isUpdate()).thenReturn(isUpdate);
        when(credentialsRequest.isForceAuthenticate()).thenReturn(isForceAuthentication);
        when(credentialsRequest.getUserAvailability()).thenReturn(userAvailability);

        // when
        MetricActionComposite loginMetric =
                (MetricActionComposite)
                        metricsFactory.createLoginMetric(credentialsRequest, interactionCounter);
        MetricAction specificLoginMetric = (MetricAction) loginMetric.getMetricActions().get(1);

        // then
        assertThat(specificLoginMetric.getActionName()).isEqualTo("login-manual");
    }
}
