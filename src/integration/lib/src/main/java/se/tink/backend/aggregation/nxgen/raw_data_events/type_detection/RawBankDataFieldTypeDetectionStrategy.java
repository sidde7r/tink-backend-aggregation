package se.tink.backend.aggregation.nxgen.raw_data_events.type_detection;

import com.fasterxml.jackson.databind.node.JsonNodeType;
import java.util.List;
import se.tink.backend.aggregation.nxgen.raw_data_events.event_producers.pojo.FieldPathPart;
import se.tink.eventproducerservice.events.grpc.RawBankDataTrackerEventProto.RawBankDataTrackerEventBankFieldType;

public interface RawBankDataFieldTypeDetectionStrategy {

    boolean isTypeMatched(List<FieldPathPart> fieldPath, String value, JsonNodeType type);

    RawBankDataTrackerEventBankFieldType getType(
            List<FieldPathPart> fieldPath, String value, JsonNodeType type);

    static boolean isStringAnInteger(String str) {
        if (str == null) {
            return false;
        }
        for (int i = 0; i < str.length(); i++) {
            if (!Character.isDigit(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    static boolean isStringADouble(String str) {
        if (str == null) {
            return false;
        }
        try {
            Double.parseDouble(str);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }
}
