package se.tink.backend.aggregation.workers.commands;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.protobuf.Any;
import java.lang.reflect.Field;
import java.util.Collections;
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
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.workers.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.AgentWorkerCommandResult;
import se.tink.backend.aggregation.workers.commands.login.BankIdLoginExceptionHandler;
import se.tink.backend.aggregation.workers.commands.login.LoginExecutor;
import se.tink.backend.aggregation.workers.metrics.MetricActionIface;
import se.tink.backend.eventproducerservice.grpc.EventAck;
import se.tink.backend.eventproducerservice.grpc.PostEventRequest;
import se.tink.eventproducerservice.events.grpc.AgentLoginCompletedEventProto.AgentLoginCompletedEvent;
import se.tink.eventproducerservice.events.grpc.AgentLoginCompletedEventProto.AgentLoginCompletedEvent.LoginResult;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.event_producer_service_client.grpc.EventProducerServiceClient;

public class LoginExecutorTest {

    private static class FakeEventProducerServiceClient implements EventProducerServiceClient {

        private Any postedData;

        @Override
        public ListenableFuture<EventAck> postEventAsync(Any data) {
            postedData = data;
            return null;
        }

        @Override
        public EventAck postEvent(PostEventRequest request) {
            throw new UnsupportedOperationException();
        }

        @Override
        public ListenableFuture<EventAck> postEventAsync(PostEventRequest request) {
            throw new UnsupportedOperationException();
        }

        Any getPostedData() {
            return Optional.ofNullable(postedData).orElseThrow(IllegalStateException::new);
        }
    }

    @Before
    public void init() throws Exception {}

    @Test
    public void eventProducerServiceShouldEmitProperEventWhenAgentGetsLoginError()
            throws Exception {

        // given
        final AgentWorkerCommandContext context = Mockito.mock(AgentWorkerCommandContext.class);
        final CredentialsRequest request = Mockito.mock(CredentialsRequest.class);
        final Credentials credentials = Mockito.mock(Credentials.class);

        Mockito.when(context.getRequest()).thenReturn(request);
        Mockito.when(request.getCredentials()).thenReturn(credentials);

        Mockito.when(credentials.getProviderName()).thenReturn("dummy-provider-name");
        Mockito.when(credentials.getUserId()).thenReturn("dummy-user-id");

        Mockito.when(context.getCorrelationId()).thenReturn("dummy-correlation-id");
        Mockito.when(context.getAppId()).thenReturn("dummy-app-id");
        Mockito.when(context.getClusterId()).thenReturn("dummy-cluster-id");

        final Agent agent = Mockito.mock(NextGenerationAgent.class);
        Mockito.doThrow(LoginError.INCORRECT_CREDENTIALS.exception()).when(agent).login();

        final FakeEventProducerServiceClient producerClient = new FakeEventProducerServiceClient();
        final LoginAgentEventProducer loginAgentEventProducer =
                new LoginAgentEventProducer(producerClient, true);

        final BankIdLoginExceptionHandler handler = Mockito.mock(BankIdLoginExceptionHandler.class);
        Mockito.when(handler.handleLoginException(Mockito.any(), Mockito.any()))
                .thenReturn(Optional.of(AgentWorkerCommandResult.ABORT));

        final LoginExecutor executor =
                new LoginExecutor(
                        Mockito.mock(StatusUpdater.class),
                        context,
                        Mockito.mock(SupplementalInformationController.class),
                        loginAgentEventProducer,
                        0);

        Field field = LoginExecutor.class.getDeclaredField("loginExceptionHandlerChain");
        field.setAccessible(true);
        field.set(executor, Collections.singletonList(handler));

        // when
        executor.executeLogin(
                agent, Mockito.mock(MetricActionIface.class), Mockito.mock(Credentials.class));

        // then
        Assert.assertEquals(
                LoginResult.LOGIN_ERROR_INCORRECT_CREDENTIALS,
                producerClient.getPostedData().unpack(AgentLoginCompletedEvent.class).getResult());
    }
}
