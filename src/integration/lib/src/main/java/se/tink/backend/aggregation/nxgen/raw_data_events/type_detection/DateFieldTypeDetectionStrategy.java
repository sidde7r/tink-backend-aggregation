package se.tink.backend.aggregation.nxgen.raw_data_events.type_detection;

import com.fasterxml.jackson.databind.node.JsonNodeType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import se.tink.backend.aggregation.nxgen.raw_data_events.event_producers.pojo.FieldPathPart;
import se.tink.eventproducerservice.events.grpc.RawBankDataTrackerEventProto.RawBankDataTrackerEventBankFieldType;

public class DateFieldTypeDetectionStrategy implements RawBankDataFieldTypeDetectionStrategy {

    private static final List<String> DATE_PATTERNS =
            Arrays.asList(
                    // (19)92-01-13 or (19)92/01/13
                    "(19|20)\\d{0,2}(-|/)(0[1-9]|1[0-2])(-|/)([0-2]\\d|3[0-1])",
                    // (19)92-13-01 or (19)92/13/01
                    "(19|20)\\d{0,2}(-|/)([0-2]\\d|3[0-1])(-|/)(0[1-9]|1[0-2])",
                    // 13-01-(19)92 or 13/01/(19)92
                    "([0-2]\\d|3[0-1])(-|/)(0[1-9]|1[0-2])(-|/)(19|20)\\d{0,2}",
                    // 01-13-(19)92 or 01/13/(19)92
                    "(0[1-9]|1[0-2])(-|/)([0-2]\\d|3[0-1])(-|/)(19|20)\\d{0,2}");

    private static final List<String> TIMESTAMP_PATTERNS =
            Arrays.asList(
                    // (T or empty space)HH:MM:SS(.mmm...)[optional](Z or +dddd or +dd:dd)[optional]
                    "(T| ){1}([0-1]\\d|2[0-3]):([0-5]\\d):([0-5]\\d)((\\.\\d{1,8})){0,1}(Z|(\\+\\d{4})|(\\+\\d{2}:\\d{2})){0,1}",
                    // No timestamp
                    "");

    private static final List<Pattern> ALL_PATTERNS;

    static {
        ALL_PATTERNS = new ArrayList<>();
        for (String datePattern : DATE_PATTERNS) {
            for (String timeStampPattern : TIMESTAMP_PATTERNS) {
                ALL_PATTERNS.add(Pattern.compile(datePattern + timeStampPattern));
            }
        }
    }

    @Override
    public boolean isTypeMatched(List<FieldPathPart> fieldPath, String value, JsonNodeType type) {
        for (Pattern p : ALL_PATTERNS) {
            if (p.matcher(value).matches()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public RawBankDataTrackerEventBankFieldType getType(
            List<FieldPathPart> fieldPath, String value, JsonNodeType type) {
        return RawBankDataTrackerEventBankFieldType.DATE;
    }
}
