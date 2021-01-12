package se.tink.backend.aggregation.events;

import com.google.protobuf.Any;
import java.time.Instant;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.eventproducerservice.events.grpc.DataTrackerEventProto.DataTrackerEvent;
import se.tink.eventproducerservice.events.grpc.DataTrackerEventProto.DataTrackerFieldInfo;
import se.tink.libraries.event_producer_service_client.grpc.EventProducerServiceClient;
import se.tink.libraries.pair.Pair;
import se.tink.libraries.serialization.proto.utils.ProtobufTypeUtil;

public class DataTrackerEventProducer {

    private final EventProducerServiceClient eventProducerServiceClient;
    private final boolean enabled;
    private static final Logger log = LoggerFactory.getLogger(DataTrackerEventProducer.class);

    @Inject
    public DataTrackerEventProducer(
            EventProducerServiceClient eventProducerServiceClient,
            @Named("sendDataTrackingEvents") boolean sendDataTrackingEvents) {
        this.eventProducerServiceClient = eventProducerServiceClient;
        this.enabled = sendDataTrackingEvents;
    }

    public void sendDataTrackerEvent(
            String providerName,
            String correlationId,
            List<Pair<String, Boolean>> data,
            String appId,
            String clusterId,
            String userId) {
        try {
            if (!enabled) {
                return;
            }

            DataTrackerEvent.Builder builder =
                    DataTrackerEvent.newBuilder()
                            .setTimestamp(ProtobufTypeUtil.toProtobufTimestamp(Instant.now()))
                            .setProviderName(providerName)
                            .setCorrelationId(correlationId)
                            .setAppId(appId)
                            .setClusterId(clusterId)
                            .setUserId(userId);

            data.stream()
                    .forEach(
                            pair ->
                                    builder.addFieldData(
                                            DataTrackerFieldInfo.newBuilder()
                                                    .setFieldName(pair.first)
                                                    .setHasValue(pair.second)
                                                    .build()));

            DataTrackerEvent event = builder.build();

            eventProducerServiceClient.postEventAsync(Any.pack(event));
        } catch (Exception e) {
            log.error("Could not push Data Tracker event", e);
        }
    }
}
