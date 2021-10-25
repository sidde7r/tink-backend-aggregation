package se.tink.backend.aggregation.nxgen.http.event.event_producers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import se.tink.eventproducerservice.events.grpc.RawBankDataTrackerEventProto;
import se.tink.eventproducerservice.events.grpc.RawBankDataTrackerEventProto.RawBankDataTrackerEvent;
import se.tink.eventproducerservice.events.grpc.RawBankDataTrackerEventProto.RawBankDataTrackerEventBankField;
import se.tink.eventproducerservice.events.grpc.RawBankDataTrackerEventProto.RefreshableItems;
import se.tink.libraries.credentials.service.RefreshableItem;

@Slf4j
public class RawBankDataEventAccumulator {

    private final List<RawBankDataTrackerEvent> eventList = new ArrayList<>();

    public void addEvent(RawBankDataTrackerEvent event, RefreshableItem refreshableItem) {
        RawBankDataTrackerEvent.Builder builder =
                RawBankDataTrackerEvent.newBuilder()
                        .setTimestamp(event.getTimestamp())
                        .setCorrelationId(event.getCorrelationId())
                        .setTraceId(Optional.ofNullable(MDC.get("traceId")).orElse("N/A"));

        if (Objects.nonNull(refreshableItem)) {
            try {
                RefreshableItems refreshableItems = mapFromRefreshableItem(refreshableItem);
                builder = builder.setRefreshableItem(refreshableItems);
            } catch (Exception e) {
                log.warn("Could not set refreshable item field in the raw bank data event");
            }
        }

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

    private RefreshableItems mapFromRefreshableItem(RefreshableItem refreshableItem) {
        switch (refreshableItem) {
            case TRANSFER_DESTINATIONS:
                return RefreshableItems.REFRESHABLE_ITEMS_TRANSFER_DESTINATIONS;
            case CHECKING_ACCOUNTS:
                return RefreshableItems.REFRESHABLE_ITEMS_CHECKING_ACCOUNTS;
            case CHECKING_TRANSACTIONS:
                return RefreshableItems.REFRESHABLE_ITEMS_CHECKING_TRANSACTIONS;
            case SAVING_ACCOUNTS:
                return RefreshableItems.REFRESHABLE_ITEMS_SAVING_ACCOUNTS;
            case SAVING_TRANSACTIONS:
                return RefreshableItems.REFRESHABLE_ITEMS_SAVING_TRANSACTIONS;
            case CREDITCARD_ACCOUNTS:
                return RefreshableItems.REFRESHABLE_ITEMS_CREDITCARD_ACCOUNTS;
            case CREDITCARD_TRANSACTIONS:
                return RefreshableItems.REFRESHABLE_ITEMS_CREDITCARD_TRANSACTIONS;
            case LOAN_ACCOUNTS:
                return RefreshableItems.REFRESHABLE_ITEMS_LOAN_ACCOUNTS;
            case LOAN_TRANSACTIONS:
                return RefreshableItems.REFRESHABLE_ITEMS_LOAN_TRANSACTIONS;
            case INVESTMENT_ACCOUNTS:
                return RefreshableItems.REFRESHABLE_ITEMS_INVESTMENT_ACCOUNTS;
            case INVESTMENT_TRANSACTIONS:
                return RefreshableItems.REFRESHABLE_ITEMS_INVESTMENT_TRANSACTIONS;
            case IDENTITY_DATA:
                return RefreshableItems.REFRESHABLE_ITEMS_IDENTITY_DATA;
            case LIST_BENEFICIARIES:
                return RefreshableItems.REFRESHABLE_ITEMS_LIST_BENEFICIARIES;
            default:
                throw new IllegalStateException(
                        "Unknown refreshable item : " + refreshableItem.name());
        }
    }
}
