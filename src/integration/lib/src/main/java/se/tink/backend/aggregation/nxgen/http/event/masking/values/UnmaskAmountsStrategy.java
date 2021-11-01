package se.tink.backend.aggregation.nxgen.http.event.masking.values;

import java.util.List;
import se.tink.backend.aggregation.nxgen.http.event.event_producers.pojo.FieldPathPart;
import se.tink.eventproducerservice.events.grpc.RawBankDataTrackerEventProto.RawBankDataTrackerEventBankFieldType;

public class UnmaskAmountsStrategy implements RawBankDataFieldValueMaskingStrategy {

    @Override
    public boolean shouldUseMaskingStrategy(
            List<FieldPathPart> fieldPathParts,
            String value,
            RawBankDataTrackerEventBankFieldType fieldType) {
        return fieldPathParts.get(fieldPathParts.size() - 1).getKeyName().equalsIgnoreCase("Amount")
                && (RawBankDataTrackerEventBankFieldType.INTEGER.equals(fieldType)
                        || RawBankDataTrackerEventBankFieldType.DOUBLE.equals(fieldType));
    }

    @Override
    public String produceMaskedValue(
            List<FieldPathPart> fieldPathParts,
            String value,
            RawBankDataTrackerEventBankFieldType fieldType) {
        return value;
    }
}
