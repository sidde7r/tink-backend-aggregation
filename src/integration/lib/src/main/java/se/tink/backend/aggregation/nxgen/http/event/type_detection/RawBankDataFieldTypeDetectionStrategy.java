package se.tink.backend.aggregation.nxgen.http.event.type_detection;

import com.fasterxml.jackson.databind.node.JsonNodeType;
import java.util.List;
import se.tink.backend.aggregation.nxgen.http.event.event_producers.pojo.FieldPathPart;
import se.tink.eventproducerservice.events.grpc.RawBankDataTrackerEventProto.RawBankDataTrackerEventBankFieldType;

public interface RawBankDataFieldTypeDetectionStrategy {

    boolean isTypeMatched(List<FieldPathPart> fieldPath, String value, JsonNodeType type);

    RawBankDataTrackerEventBankFieldType getType(
            List<FieldPathPart> fieldPath, String value, JsonNodeType type);
}
