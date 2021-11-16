package se.tink.backend.aggregation.nxgen.raw_data_events.type_detection.account_identifier_detectors;

import com.fasterxml.jackson.databind.node.JsonNodeType;
import java.util.List;
import se.tink.backend.aggregation.nxgen.raw_data_events.event_producers.pojo.FieldPathPart;
import se.tink.backend.aggregation.nxgen.raw_data_events.type_detection.RawBankDataFieldTypeDetectionStrategy;
import se.tink.eventproducerservice.events.grpc.RawBankDataTrackerEventProto.RawBankDataTrackerEventBankFieldType;

public class MaskedPanFieldTypeDetectionStrategy implements RawBankDataFieldTypeDetectionStrategy {

    @Override
    public boolean isTypeMatched(List<FieldPathPart> fieldPath, String value, JsonNodeType type) {
        if (!JsonNodeType.STRING.equals(type)) {
            return false;
        }
        return isProbablyMaskedPan(value);
    }

    @Override
    public RawBankDataTrackerEventBankFieldType getType(
            List<FieldPathPart> fieldPath, String value, JsonNodeType type) {
        return RawBankDataTrackerEventBankFieldType.MASKED_PAN;
    }

    private static boolean isProbablyMaskedPan(String input) {
        // Any string with more than 10 digits is potentially an unmasked PAN and treated as such
        if (input.replaceAll("[^\\d]", "").length() > 10) {
            return false;
        }

        for (int i = 0; i < input.length(); i++) {
            char c = input.toLowerCase().charAt(i);
            if (!Character.isDigit(c) && c != ' ' && c != 'x' && c != '*' && c != '-') {
                return false;
            }
        }

        // Masked PANs can consist of numbers, X, *, dashes and spaces
        // All other characters will be stripped
        String cleaned = input.replaceAll("[^\\dXx* -]", "");
        String numbers = input.replaceAll("[^\\d]", "");

        // At least 4 digits required
        if (numbers.length() < 4 || input.length() < 12) {
            return false;
        }

        // Any string that contains input other than the whitelisted characters is invalid
        return cleaned.length() == input.length();
    }
}
