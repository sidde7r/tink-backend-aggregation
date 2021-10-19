package se.tink.backend.aggregation.nxgen.http.event.configuration;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import se.tink.backend.aggregation.nxgen.http.event.masking.keys.MaskKeysWithNumericValuesStrategy;
import se.tink.backend.aggregation.nxgen.http.event.masking.keys.RawBankDataKeyValueMaskingStrategy;
import se.tink.backend.aggregation.nxgen.http.event.masking.values.MaskAllValueMaskingStrategy;
import se.tink.backend.aggregation.nxgen.http.event.masking.values.RawBankDataFieldValueMaskingStrategy;
import se.tink.backend.aggregation.nxgen.http.event.masking.values.UnmaskDateValueMaskingStrategy;
import se.tink.backend.aggregation.nxgen.http.event.type_detection.DateFieldTypeDetectionStrategy;
import se.tink.backend.aggregation.nxgen.http.event.type_detection.DummyFieldTypeDetectionStrategy;
import se.tink.backend.aggregation.nxgen.http.event.type_detection.RawBankDataFieldTypeDetectionStrategy;

@Builder
@Getter
public class RawBankDataEventCreationStrategies {

    private final List<RawBankDataKeyValueMaskingStrategy> keyMaskingStrategies;
    private final List<RawBankDataFieldValueMaskingStrategy> valueMaskingStrategies;
    private final List<RawBankDataFieldTypeDetectionStrategy> fieldTypeDetectionStrategies;

    public static RawBankDataEventCreationStrategies createDefaultConfiguration() {
        return RawBankDataEventCreationStrategies.builder()
                .keyMaskingStrategies(
                        Collections.singletonList(new MaskKeysWithNumericValuesStrategy()))
                .valueMaskingStrategies(
                        Arrays.asList(
                                new UnmaskDateValueMaskingStrategy(),
                                new MaskAllValueMaskingStrategy()))
                .fieldTypeDetectionStrategies(
                        Arrays.asList(
                                new DateFieldTypeDetectionStrategy(),
                                new DummyFieldTypeDetectionStrategy()))
                .build();
    }
}
