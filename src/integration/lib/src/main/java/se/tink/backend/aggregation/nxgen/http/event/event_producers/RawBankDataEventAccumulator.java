package se.tink.backend.aggregation.nxgen.http.event.event_producers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import se.tink.eventproducerservice.events.grpc.RawBankDataTrackerEventProto;
import se.tink.eventproducerservice.events.grpc.RawBankDataTrackerEventProto.RawBankDataTrackerEvent;
import se.tink.eventproducerservice.events.grpc.RawBankDataTrackerEventProto.RawBankDataTrackerEventBankField;

@Slf4j
public class RawBankDataEventAccumulator {

    private final List<RawBankDataTrackerEvent> eventList = new ArrayList<>();
    private long totalProcessingTime = 0;

    public void addEvent(RawBankDataTrackerEvent event) {
        long startTime = System.currentTimeMillis();
        RawBankDataTrackerEvent.Builder builder =
                RawBankDataTrackerEvent.newBuilder()
                        .setTimestamp(event.getTimestamp())
                        .setCorrelationId(event.getCorrelationId());

        Map<RawBankDataTrackerEventBankField, Integer> fieldCount = new HashMap<>();
        for (RawBankDataTrackerEventProto.RawBankDataTrackerEventBankField field :
                event.getFieldDataList()) {
            if (!fieldCount.containsKey(field)) {
                fieldCount.put(field, 1);
            } else {
                fieldCount.put(field, fieldCount.get(field) + 1);
            }
        }
        for (RawBankDataTrackerEventBankField field : fieldCount.keySet()) {
            RawBankDataTrackerEventBankField newField =
                    RawBankDataTrackerEventBankField.newBuilder()
                            .setFieldPath(field.getFieldPath())
                            .setFieldType(field.getFieldType())
                            .setIsFieldSet(field.getIsFieldSet())
                            .setIsFieldMasked(field.getIsFieldMasked())
                            .setFieldValue(field.getFieldValue())
                            .setCount(fieldCount.get(field))
                            .build();
            builder.addFieldData(newField);
        }
        RawBankDataTrackerEvent aggregatedEvent = builder.build();
        this.eventList.add(aggregatedEvent);
        long endTime = System.currentTimeMillis();
        this.totalProcessingTime += endTime - startTime;
    }

    public List<RawBankDataTrackerEvent> getEventList() {
        log.info(
                "[RawBankDataEventAccumulator] Spent {} milliseconds for raw bank data event processing",
                this.totalProcessingTime);
        return eventList;
    }
}
