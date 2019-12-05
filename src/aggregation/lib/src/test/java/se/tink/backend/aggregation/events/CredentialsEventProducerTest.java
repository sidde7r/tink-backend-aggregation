package se.tink.backend.aggregation.events;

import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import java.util.Collections;
import java.util.Set;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.eventproducerservice.events.grpc.CredentialsRefreshCommandChainStartedProto.CredentialsRefreshCommandChainStarted;
import se.tink.libraries.event_producer_service_client.grpc.EventProducerServiceClient;
import se.tink.libraries.uuid.UUIDUtils;

@RunWith(MockitoJUnitRunner.class)
public class CredentialsEventProducerTest {
    private static final String CORRELATION_ID = "correlationId";
    private static final String CLUSTER_ID = "clusterId";
    private static final boolean MANUAL = true;
    private static final Set<CredentialsRefreshCommandChainStarted.RefreshableItems>
            REFRESHABLE_ITEMS = Collections.emptySet();
    private EventProducerServiceClient eventProducerServiceClient;
    private CredentialsEventProducer credentialsEventProducer;
    private Credentials validCredentials;
    private String appId;

    @Before
    public void setup() {
        this.eventProducerServiceClient = Mockito.mock(EventProducerServiceClient.class);
        this.credentialsEventProducer = new CredentialsEventProducer(eventProducerServiceClient);
        this.validCredentials = buildValidCredentials();
        this.appId = UUIDUtils.generateUUID();
    }

    @Test
    public void testCredentialsRefreshCommandChainStarted() throws InvalidProtocolBufferException {
        ArgumentCaptor<Any> capture = ArgumentCaptor.forClass(Any.class);
        credentialsEventProducer.sendCredentialsRefreshCommandChainStarted(
                validCredentials,
                appId,
                CORRELATION_ID,
                CLUSTER_ID,
                MANUAL,
                Collections.emptySet());
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
                        .setCorrelationId(CORRELATION_ID)
                        .setClusterId(CLUSTER_ID)
                        .setManual(MANUAL)
                        .addAllRefreshableItems(REFRESHABLE_ITEMS)
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
