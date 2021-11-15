package se.tink.backend.aggregation.nxgen.raw_data_events.event_producers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import se.tink.eventproducerservice.events.grpc.RawBankDataTrackerEventProto;
import se.tink.eventproducerservice.events.grpc.RawBankDataTrackerEventProto.RawBankDataTrackerEvent;
import se.tink.eventproducerservice.events.grpc.RawBankDataTrackerEventProto.RawBankDataTrackerEventBankField;
import se.tink.libraries.credentials.service.RefreshableItem;

@Slf4j
public class RawBankDataEventAccumulator {

    // If we have that many identical rows when we ignore "offset" field, we ignore offset
    // field and compress them (send 1 row instead of >= 30 and put "count" data in the row)
    private static final int COMPRESSION_THRESHOLD = 30;
    private final List<RawBankDataTrackerEvent> eventList = new ArrayList<>();

    public void addEvent(RawBankDataTrackerEvent event, RefreshableItem refreshableItem) {
        RawBankDataTrackerEvent.Builder builder =
                RawBankDataTrackerEvent.newBuilder()
                        .setTimestamp(event.getTimestamp())
                        .setCorrelationId(event.getCorrelationId())
                        .setTraceId(Optional.ofNullable(MDC.get("traceId")).orElse("N/A"))
                        .setProviderName(event.getProviderName())
                        .setEventId(UUID.randomUUID().toString());

        if (Objects.nonNull(refreshableItem)) {
            try {
                builder = builder.setRefreshableItem(refreshableItem.name());
            } catch (Exception e) {
                log.warn("Could not set refreshable item field in the raw bank data event");
            }
        }

        // Preprocessing1: Ignore offset column and then detect identical rows and their count
        Map<String, Integer> fieldCount = new HashMap<>();
        for (RawBankDataTrackerEventProto.RawBankDataTrackerEventBankField field :
                event.getFieldDataList()) {
            String stringifiedRow = getRowStringIgnoreOffsetColumn(field);
            if (!fieldCount.containsKey(stringifiedRow)) {
                fieldCount.put(stringifiedRow, 1);
            } else {
                fieldCount.put(stringifiedRow, fieldCount.get(stringifiedRow) + 1);
            }
        }

        // Preprocessing2: For rows that repeats a lot (when we ignore "offset" column)
        // Create a Set that tells that such rows should be compressed and a Map to
        // indicate whether a compressed version is added to the aggregated event or not
        Set<String> rowsToCompress =
                fieldCount.entrySet().stream()
                        .filter(entry -> entry.getValue() >= COMPRESSION_THRESHOLD)
                        .map(Entry::getKey)
                        .collect(Collectors.toSet());
        Map<String, Boolean> isAggregatedRowAdded = new HashMap<>();
        for (String row : rowsToCompress) {
            isAggregatedRowAdded.put(row, false);
        }

        // Processing: Iterate on all fields, if we need to compress add compressed version if
        // it is not added before (ignore offset). Otherwise add uncompressed row (with offset)
        for (RawBankDataTrackerEventBankField field : event.getFieldDataList()) {
            String stringifiedRow = getRowStringIgnoreOffsetColumn(field);
            if (rowsToCompress.contains(stringifiedRow)) {
                Boolean added = isAggregatedRowAdded.get(stringifiedRow);
                if (Boolean.FALSE.equals(added)) {
                    builder.addFieldData(
                            createBuilderToClone(field)
                                    .setCount(fieldCount.get(stringifiedRow))
                                    .build());
                    isAggregatedRowAdded.put(stringifiedRow, true);
                }
            } else {
                builder.addFieldData(
                        createBuilderToClone(field)
                                .setOffset(field.getOffset())
                                .setCount(1)
                                .build());
            }
        }
        RawBankDataTrackerEvent aggregatedEvent = builder.build();
        this.eventList.add(aggregatedEvent);
    }

    public List<RawBankDataTrackerEvent> getEventList() {
        return eventList;
    }

    private String getRowStringIgnoreOffsetColumn(RawBankDataTrackerEventBankField field) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(field.getFieldPath());
        stringBuilder.append("#");
        stringBuilder.append(field.getFieldType());
        stringBuilder.append("#");
        stringBuilder.append(field.getIsFieldSet());
        stringBuilder.append("#");
        stringBuilder.append(field.getIsFieldMasked());
        stringBuilder.append("#");
        stringBuilder.append(field.getFieldValue());
        return stringBuilder.toString();
    }

    private RawBankDataTrackerEventBankField.Builder createBuilderToClone(
            RawBankDataTrackerEventBankField field) {
        return RawBankDataTrackerEventBankField.newBuilder()
                .setFieldPath(field.getFieldPath())
                .setFieldType(field.getFieldType())
                .setIsFieldSet(field.getIsFieldSet())
                .setIsFieldMasked(field.getIsFieldMasked())
                .setFieldValue(field.getFieldValue());
    }
}
