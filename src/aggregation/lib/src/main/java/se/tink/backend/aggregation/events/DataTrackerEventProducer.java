package se.tink.backend.aggregation.events;

import com.google.protobuf.Any;
import java.time.Instant;
import javax.inject.Inject;
import se.tink.eventproducerservice.events.grpc.DataTrackerEventProto.DataTrackerEvent;
import se.tink.libraries.event_producer_service_client.grpc.EventProducerServiceClient;
import se.tink.libraries.serialization.proto.utils.ProtobufTypeUtil;

public class DataTrackerEventProducer {

    private final EventProducerServiceClient eventProducerServiceClient;

    @Inject
    public DataTrackerEventProducer(EventProducerServiceClient eventProducerServiceClient) {
        this.eventProducerServiceClient = eventProducerServiceClient;
    }

    public void sendDataTrackerEvent(
            String providerName,
            String correlationId,
            String fieldName,
            boolean hasValue,
            String appId,
            String clusterId,
            String userId) {
        DataTrackerEvent event =
                DataTrackerEvent.newBuilder()
                        .setTimestamp(ProtobufTypeUtil.toProtobufTimestamp(Instant.now()))
                        .setProviderName(providerName)
                        .setCorrelationId(correlationId)
                        .setFieldName(fieldName)
                        .setHasValue(hasValue)
                        .setAppId(appId)
                        .setClusterId(clusterId)
                        .setUserId(userId)
                        .build();

        eventProducerServiceClient.postEventAsync(Any.pack(event));
    }
}
