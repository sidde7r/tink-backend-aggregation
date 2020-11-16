package se.tink.backend.aggregation.workers.commands.login;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.protobuf.Any;
import java.util.List;
import java.util.Optional;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.agent.Agent;
import se.tink.backend.aggregation.agents.contexts.StatusUpdater;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.events.LoginAgentEventProducer;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.workers.context.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.metrics.MetricActionIface;
import se.tink.backend.eventproducerservice.grpc.BatchEventAck;
import se.tink.backend.eventproducerservice.grpc.BatchEventAckAsync;
import se.tink.backend.eventproducerservice.grpc.EventAck;
import se.tink.backend.eventproducerservice.grpc.EventAckAsync;
import se.tink.eventproducerservice.events.grpc.AgentLoginCompletedEventProto.AgentLoginCompletedEvent;
import se.tink.eventproducerservice.events.grpc.AgentLoginCompletedEventProto.AgentLoginCompletedEvent.LoginResult;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.event_producer_service_client.grpc.EventProducerServiceClient;
import se.tink.libraries.i18n.Catalog;

public class LoginExecutorTest {

    private static class FakeEventProducerServiceClient implements EventProducerServiceClient {

        private Any postedData;

        @Override
        public ListenableFuture<EventAck> postEventAsync(Any data) {
            postedData = data;
            return null;
        }

        @Override
        public ListenableFuture<EventAckAsync> postEventFireAndForget(Any data) {
            throw new UnsupportedOperationException();
        }

        @Override
        public ListenableFuture<BatchEventAck> postEventsBatchAsync(List<Any> data) {
            throw new UnsupportedOperationException();
        }

        @Override
        public ListenableFuture<BatchEventAckAsync> postEventsBatchFireAndForget(List<Any> data) {
            throw new UnsupportedOperationException();
        }

        Any getPostedData() {
            return Optional.ofNullable(postedData).orElseThrow(IllegalStateException::new);
        }
    }

    private LoginExecutor objectUnderTest;
    private FakeEventProducerServiceClient producerClient;
    private MetricActionIface metricAction;
    private CredentialsRequest credentialsRequest;

    @Before
    public void init() throws Exception {
        metricAction = Mockito.mock(MetricActionIface.class);
        credentialsRequest = Mockito.mock(CredentialsRequest.class);

        final AgentWorkerCommandContext context = Mockito.mock(AgentWorkerCommandContext.class);
        final CredentialsRequest request = Mockito.mock(CredentialsRequest.class);
        final Credentials credentials = Mockito.mock(Credentials.class);

        Mockito.when(context.getRequest()).thenReturn(request);
        Mockito.when(request.getCredentials()).thenReturn(credentials);

        Mockito.when(credentials.getProviderName()).thenReturn("dummy-provider-name");
        Mockito.when(credentials.getUserId()).thenReturn("dummy-user-id");

        Mockito.when(context.getCorrelationId()).thenReturn("dummy-correlation-id");
        Mockito.when(context.getAppId()).thenReturn("dummy-app-id");
        Mockito.when(context.getCatalog()).thenReturn(Mockito.mock(Catalog.class));
        Mockito.when(context.getClusterId()).thenReturn("dummy-cluster-id");

        SupplementalInformationControllerUsageMonitorProxy
                supplementalInformationControllerUsageMonitorProxy =
                        Mockito.mock(SupplementalInformationControllerUsageMonitorProxy.class);
        producerClient = new FakeEventProducerServiceClient();
        final LoginAgentEventProducer loginAgentEventProducer =
                new LoginAgentEventProducer(producerClient, true);
        final AgentLoginEventPublisherService agentLoginEventPublisherService =
                new AgentLoginEventPublisherService(
                        loginAgentEventProducer,
                        0,
                        context,
                        supplementalInformationControllerUsageMonitorProxy);

        objectUnderTest =
                new LoginExecutor(
                        Mockito.mock(StatusUpdater.class),
                        context,
                        Mockito.mock(SupplementalInformationControllerUsageMonitorProxy.class),
                        agentLoginEventPublisherService);
    }

    @Test
    public void eventProducerServiceShouldEmitProperEventWhenAgentGetsLoginError()
            throws Exception {

        // given
        final Agent agent = Mockito.mock(NextGenerationAgent.class);
        Mockito.doThrow(LoginError.INCORRECT_CREDENTIALS.exception()).when(agent).login();

        // when
        objectUnderTest.executeLogin(agent, metricAction, credentialsRequest);

        // then
        Assert.assertEquals(
                LoginResult.LOGIN_ERROR_INCORRECT_CREDENTIALS,
                producerClient.getPostedData().unpack(AgentLoginCompletedEvent.class).getResult());
    }
}
