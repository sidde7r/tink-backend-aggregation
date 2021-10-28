package se.tink.backend.aggregation.nxgen.http.event.type_detection.account_identifier_detectors;

import com.fasterxml.jackson.databind.node.JsonNodeType;
import java.util.List;
import se.tink.backend.aggregation.nxgen.http.event.event_producers.pojo.FieldPathPart;
import se.tink.backend.aggregation.nxgen.http.event.type_detection.RawBankDataFieldTypeDetectionStrategy;
import se.tink.eventproducerservice.events.grpc.RawBankDataTrackerEventProto.RawBankDataTrackerEventBankFieldType;

public class StringFieldTypeDetectionStrategy implements RawBankDataFieldTypeDetectionStrategy {

    @Override
    public boolean isTypeMatched(List<FieldPathPart> fieldPath, String value, JsonNodeType type) {
        return JsonNodeType.STRING.equals(type);
    }

    @Override
    public RawBankDataTrackerEventBankFieldType getType(
            List<FieldPathPart> fieldPath, String value, JsonNodeType type) {
        return RawBankDataTrackerEventBankFieldType.STRING;
    }
}
