package se.tink.backend.aggregation.events;

import com.google.common.base.Strings;
import com.google.protobuf.Any;
import java.time.Instant;
import javax.inject.Inject;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.eventproducerservice.events.grpc.CredentialsRefreshCommandChainStartedProto.CredentialsRefreshCommandChainStarted;
import se.tink.libraries.event_producer_service_client.grpc.EventProducerServiceClient;
import se.tink.libraries.serialization.proto.utils.ProtobufTypeUtil;

public class CredentialsEventProducer {

    private final EventProducerServiceClient eventProducerServiceClient;

    @Inject
    public CredentialsEventProducer(EventProducerServiceClient eventProducerServiceClient) {
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

        eventProducerServiceClient.postEventAsync(Any.pack(data));
    }
}
