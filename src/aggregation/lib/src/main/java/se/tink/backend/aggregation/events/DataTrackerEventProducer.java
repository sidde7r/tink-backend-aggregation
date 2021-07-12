package se.tink.backend.aggregation.events;

import com.google.protobuf.Message;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.eventproducerservice.events.grpc.DataTrackerEventProto.DataTrackerEvent;
import se.tink.eventproducerservice.events.grpc.DataTrackerEventProto.DataTrackerFieldInfo;
import se.tink.libraries.serialization.proto.utils.ProtobufTypeUtil;

public class DataTrackerEventProducer {

    private final boolean enabled;
    private static final Logger log = LoggerFactory.getLogger(DataTrackerEventProducer.class);

    @Inject
    public DataTrackerEventProducer(
            @Named("sendDataTrackingEvents") boolean sendDataTrackingEvents) {
        this.enabled = sendDataTrackingEvents;
    }

    public List<Message> toMessages(List<DataTrackerEvent> events) {
        if (!enabled) {
            return Collections.emptyList();
        }
        try {
            return events.stream().map(Message.class::cast).collect(Collectors.toList());
        } catch (Exception e) {
            log.warn(
                    "Failed to convert to data-tracker event from messages. Cause: {}",
                    ExceptionUtils.getStackTrace(e));
        }
        return Collections.emptyList();
    }

    public DataTrackerEvent produceDataTrackerEvent(
            String providerName,
            String correlationId,
            Map<String, Boolean> isFieldPopulated,
            Map<String, String> fieldValues,
            String appId,
            String clusterId,
            String userId) {

        DataTrackerEvent.Builder builder =
                DataTrackerEvent.newBuilder()
                        .setTimestamp(ProtobufTypeUtil.toProtobufTimestamp(Instant.now()))
                        .setProviderName(providerName)
                        .setCorrelationId(correlationId)
                        .setAppId(appId)
                        .setClusterId(clusterId)
                        .setUserId(userId);

        isFieldPopulated.forEach(
                (fieldName, isPopulated) ->
                        builder.addFieldData(
                                DataTrackerFieldInfo.newBuilder()
                                        .setFieldName(fieldName)
                                        .setHasValue(isPopulated)
                                        .setValue(fieldValues.getOrDefault(fieldName, ""))
                                        .build()));
        return builder.build();
    }
}
