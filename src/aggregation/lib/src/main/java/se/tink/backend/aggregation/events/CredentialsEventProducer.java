package se.tink.backend.aggregation.events;

import com.google.common.base.Strings;
import com.google.protobuf.Any;
import java.time.Instant;
import javax.annotation.Nullable;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.eventproducerservice.events.grpc.CredentialsRefreshCommandChainStartedProto.CredentialsRefreshCommandChainStarted;
import se.tink.libraries.event_producer_service_client.grpc.EventProducerServiceClient;
import se.tink.libraries.serialization.proto.utils.ProtobufTypeUtil;

// import se.tink.backend.agents.rpc.Credentials;
// import se.tink.libraries.event_producer_service_client.grpc.EventProducerServiceClient;

public class CredentialsEventProducer {

    private static final Logger logger = LoggerFactory.getLogger(CredentialsEventProducer.class);
    private final EventProducerServiceClient eventProducerServiceClient;

    @Inject
    public CredentialsEventProducer(
            @Nullable EventProducerServiceClient eventProducerServiceClient) {
        this.eventProducerServiceClient = eventProducerServiceClient;
    }

    public void sendCredentialsRefreshCommandChainStarted(Credentials credentials, String appId) {
        CredentialsRefreshCommandChainStarted data =
                CredentialsRefreshCommandChainStarted.newBuilder()
                        .setTimestamp(ProtobufTypeUtil.toProtobufTimestamp(Instant.now()))
                        .setAppId(Strings.nullToEmpty(appId))
                        .setUserId(credentials.getUserId())
                        .setCredentialsId(credentials.getId())
                        .setProviderName(credentials.getProviderName())
                        .build();

        sendEvent(Any.pack(data));
    }

    private void sendEvent(Any data) {
        if (eventProducerServiceClient == null) {
            logger.info(
                    "No EventProducerService service is configured. Skipping posting of event.");
            return;
        }
        eventProducerServiceClient.postEventAsync(data);
    }
}
