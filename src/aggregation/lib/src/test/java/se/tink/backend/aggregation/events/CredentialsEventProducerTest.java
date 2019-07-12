package se.tink.backend.aggregation.events;

import com.google.protobuf.Any;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.workers.AgentWorkerCommandResult;
import se.tink.eventproducerservice.events.grpc.CredentialsRefreshCommandChainStartedProto.CredentialsRefreshCommandChainStarted;
import se.tink.libraries.event_producer_service_client.grpc.EventProducerServiceClient;
import se.tink.libraries.uuid.UUIDUtils;

public class CredentialsEventProducerTest {

    private Credentials validCredentials;
    private EventProducerServiceClient eventProducerServiceClient;
    private String appId;

    @Before
    public void setup() {
        this.eventProducerServiceClient = Mockito.mock(EventProducerServiceClient.class);
        this.validCredentials = buildValidCredentials();
        this.appId = UUIDUtils.generateUUID();
    }

    @Test
    public void
            testCredentialsRefreshCommandChainStarted_WhenClientIsAvailable_EventShouldBePosted()
                    throws Exception {

        ArgumentCaptor<Any> capture = ArgumentCaptor.forClass(Any.class);

        CredentialsEventProducer credentialsEventProducer =
                new CredentialsEventProducer(eventProducerServiceClient, validCredentials, appId);

        Assertions.assertThat(credentialsEventProducer.execute())
                .isEqualTo(AgentWorkerCommandResult.CONTINUE);

        Mockito.verify(eventProducerServiceClient, Mockito.times(1))
                .postEventAsync(capture.capture());

        CredentialsRefreshCommandChainStarted output =
                capture.getValue().unpack(CredentialsRefreshCommandChainStarted.class);

        CredentialsRefreshCommandChainStarted data =
                CredentialsRefreshCommandChainStarted.newBuilder()
                        .setTimestamp(output.getTimestamp())
                        .setAppId(appId)
                        .setUserId(validCredentials.getUserId())
                        .setCredentialsId(validCredentials.getId())
                        .setProviderName(validCredentials.getProviderName())
                        .build();

        Assert.assertEquals(data, output);
    }

    private Credentials buildValidCredentials() {
        Credentials credentials = new Credentials();
        credentials.setId(UUIDUtils.generateUUID());
        credentials.setUserId(UUIDUtils.generateUUID());
        credentials.setProviderName(UUIDUtils.generateUUID());
        return credentials;
    }
}
