package se.tink.backend.integration.agent_data_availability_tracker.client.grpc;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Scopes;
import io.grpc.ManagedChannel;
import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.models.AccountFeatures;
import se.tink.backend.integration.agent_data_availability_tracker.client.AgentDataAvailabilityTrackerClient;
import se.tink.backend.integration.agent_data_availability_tracker.client.AgentDataAvailabilityTrackerClientImpl;
import se.tink.backend.integration.agent_data_availability_tracker.serialization.AccountTrackingSerializer;
import se.tink.backend.integration.agent_data_availability_tracker.serialization.SerializationUtils;

public final class AgentDataAvailabilityTrackerClientGrpcTest {

    @Test
    public void sendAccountWhenSendingAgentTheServerShouldReceiveTheSameAgent() throws Exception {
        // given
        final CompletableFuture<String> future = new CompletableFuture<>();
        final Account account = new Account();
        final AccountFeatures accountFeatures = new AccountFeatures();

        final GrpcServerService serverService = new GrpcServerService(future);
        serverService.start();

        final Injector injector = Guice.createInjector(new TestModule(serverService));
        final AgentDataAvailabilityTrackerClient client =
                injector.getInstance(AgentDataAvailabilityTrackerClient.class);
        client.start();

        // when
        AccountTrackingSerializer serializer =
                SerializationUtils.serializeAccount(account, accountFeatures);

        client.sendAccount("TestBank", "test-test", "SE", serializer);

        // then
        Assert.assertEquals("TestBank", future.get());
    }

    private static class TestModule extends AbstractModule {

        private final GrpcServerService serverService;

        private TestModule(GrpcServerService serverService) {
            this.serverService = serverService;
        }

        @Override
        protected void configure() {
            bind(AgentDataAvailabilityTrackerClient.class)
                    .to(AgentDataAvailabilityTrackerClientImpl.class)
                    .in(Scopes.SINGLETON);
            bind(ManagedChannel.class).toProvider(PlaintextChannelProvider.class);
            bind(InetSocketAddress.class)
                    .toInstance(new InetSocketAddress("127.0.0.1", serverService.getPort()));
        }
    }
}
