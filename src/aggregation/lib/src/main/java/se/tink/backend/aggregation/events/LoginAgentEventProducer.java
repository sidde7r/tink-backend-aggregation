package se.tink.backend.aggregation.events;

import com.google.protobuf.Any;
import java.time.Instant;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.eventproducerservice.events.grpc.AgentLoginCompletedEventProto.AgentLoginCompletedEvent;
import se.tink.eventproducerservice.events.grpc.AgentLoginCompletedEventProto.AgentLoginCompletedEvent.LoginResultReason;
import se.tink.libraries.event_producer_service_client.grpc.EventProducerServiceClient;
import se.tink.libraries.serialization.proto.utils.ProtobufTypeUtil;

public class LoginAgentEventProducer {
    private final EventProducerServiceClient eventProducerServiceClient;
    private static final Logger log = LoggerFactory.getLogger(LoginAgentEventProducer.class);

    @Inject
    public LoginAgentEventProducer(EventProducerServiceClient eventProducerServiceClient) {
        this.eventProducerServiceClient = eventProducerServiceClient;
    }

    public void sendLoginCompletedEvent(
            String providerName,
            String correlationId,
            LoginResultReason reason,
            float elapsedTime,
            String appId,
            String clusterId,
            String userId) {
        try {
            AgentLoginCompletedEvent event =
                    AgentLoginCompletedEvent.newBuilder()
                            .setTimestamp(ProtobufTypeUtil.toProtobufTimestamp(Instant.now()))
                            .setProviderName(providerName)
                            .setCorrelationId(correlationId)
                            .setReason(reason)
                            .setElapsedTime(elapsedTime)
                            .setAppId(appId)
                            .setClusterId(clusterId)
                            .setUserId(userId)
                            .build();

            eventProducerServiceClient.postEventAsync(Any.pack(event));
        } catch (Exception e) {
            log.error("Could not push agent login event", e);
        }
    }
}
