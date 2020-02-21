package se.tink.backend.integration.agent_data_availability_tracker.client.grpc;

import java.util.concurrent.CompletableFuture;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.models.AccountFeatures;
import se.tink.backend.integration.agent_data_availability_tracker.client.AgentDataAvailabilityTrackerClient;
import se.tink.backend.integration.agent_data_availability_tracker.client.AgentDataAvailabilityTrackerClientImpl;

public final class AgentDataAvailabilityTrackerClientGrpcTest {

    @Test
    public void sendAccountWhenSendingAgentTheServerShouldReceiveTheSameAgent() throws Exception {
        // given
        final CompletableFuture<String> future = new CompletableFuture<>();
        final Account account = new Account();
        final AccountFeatures accountFeatures = new AccountFeatures();

        final GrpcServerService serverService = new GrpcServerService(future);
        serverService.start();

        final AgentDataAvailabilityTrackerClient client =
                new AgentDataAvailabilityTrackerClientImpl("127.0.0.1", serverService.getPort());
        client.start();

        // when
        client.sendAccount("TestBank", "test-test", "SE", account, accountFeatures);

        // then
        Assert.assertEquals("TestBank", future.get());
    }
}
