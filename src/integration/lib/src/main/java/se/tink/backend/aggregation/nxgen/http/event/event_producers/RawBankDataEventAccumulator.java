package se.tink.backend.aggregation.nxgen.http.event.event_producers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import se.tink.eventproducerservice.events.grpc.RawBankDataTrackerEventProto;
import se.tink.eventproducerservice.events.grpc.RawBankDataTrackerEventProto.RawBankDataTrackerEvent;
import se.tink.eventproducerservice.events.grpc.RawBankDataTrackerEventProto.RawBankDataTrackerEventBankField;

public class RawBankDataEventAccumulator {

    private final List<RawBankDataTrackerEvent> eventList = new ArrayList<>();

    public void addEvent(RawBankDataTrackerEvent event) {
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
    }

    public List<RawBankDataTrackerEvent> getEventList() {
        return eventList;
    }
}
