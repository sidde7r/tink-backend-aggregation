package se.tink.backend.aggregation.nxgen.http.event.type_detection.account_identifier_detectors;

import com.fasterxml.jackson.databind.node.JsonNodeType;
import java.util.List;
import se.tink.backend.aggregation.nxgen.http.event.event_producers.pojo.FieldPathPart;
import se.tink.backend.aggregation.nxgen.http.event.type_detection.RawBankDataFieldTypeDetectionStrategy;
import se.tink.eventproducerservice.events.grpc.RawBankDataTrackerEventProto.RawBankDataTrackerEventBankFieldType;

public class BooleanFieldTypeDetectionStrategy implements RawBankDataFieldTypeDetectionStrategy {

    @Override
    public boolean isTypeMatched(List<FieldPathPart> fieldPath, String value, JsonNodeType type) {
        return JsonNodeType.BOOLEAN.equals(type)
                || "true".equalsIgnoreCase(value)
                || "false".equalsIgnoreCase(value);
    }

    @Override
    public RawBankDataTrackerEventBankFieldType getType(
            List<FieldPathPart> fieldPath, String value, JsonNodeType type) {
        return RawBankDataTrackerEventBankFieldType.BOOLEAN;
    }
}
