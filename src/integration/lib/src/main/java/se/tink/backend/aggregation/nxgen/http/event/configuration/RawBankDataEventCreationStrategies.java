package se.tink.backend.aggregation.nxgen.http.event.configuration;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import se.tink.backend.aggregation.nxgen.http.event.masking.keys.MaskKeysWithNumericValuesStrategy;
import se.tink.backend.aggregation.nxgen.http.event.masking.keys.RawBankDataKeyValueMaskingStrategy;
import se.tink.backend.aggregation.nxgen.http.event.masking.values.GenericMaskingStrategy;
import se.tink.backend.aggregation.nxgen.http.event.masking.values.MaskAllValuesMaskingStrategy;
import se.tink.backend.aggregation.nxgen.http.event.masking.values.RawBankDataFieldValueMaskingStrategy;
import se.tink.backend.aggregation.nxgen.http.event.masking.values.UnmaskBooleanValueMaskingStrategy;
import se.tink.backend.aggregation.nxgen.http.event.masking.values.UnmaskDateValueMaskingStrategy;
import se.tink.backend.aggregation.nxgen.http.event.masking.values.UnmaskEnumsStrategy;
import se.tink.backend.aggregation.nxgen.http.event.type_detection.BooleanFieldTypeDetectionStrategy;
import se.tink.backend.aggregation.nxgen.http.event.type_detection.DateFieldTypeDetectionStrategy;
import se.tink.backend.aggregation.nxgen.http.event.type_detection.DoubleFieldTypeDetectionStrategy;
import se.tink.backend.aggregation.nxgen.http.event.type_detection.DummyFieldTypeDetectionStrategy;
import se.tink.backend.aggregation.nxgen.http.event.type_detection.IntegerFieldTypeDetectionStrategy;
import se.tink.backend.aggregation.nxgen.http.event.type_detection.RawBankDataFieldTypeDetectionStrategy;
import se.tink.backend.aggregation.nxgen.http.event.type_detection.StringFieldTypeDetectionStrategy;
import se.tink.backend.aggregation.nxgen.http.event.type_detection.account_identifier_detectors.BbanFieldTypeDetectionStrategy;
import se.tink.backend.aggregation.nxgen.http.event.type_detection.account_identifier_detectors.IbanFieldTypeDetectionStrategy;
import se.tink.backend.aggregation.nxgen.http.event.type_detection.account_identifier_detectors.MaskedPanFieldTypeDetectionStrategy;
import se.tink.backend.aggregation.nxgen.http.event.type_detection.account_identifier_detectors.SortCodeFieldTypeDetectionStrategy;

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
                                // order is important!
                                new UnmaskEnumsStrategy(),
                                new UnmaskDateValueMaskingStrategy(),
                                new UnmaskBooleanValueMaskingStrategy(),
                                new GenericMaskingStrategy(),
                                new MaskAllValuesMaskingStrategy()))
                .fieldTypeDetectionStrategies(
                        // order is important!
                        Arrays.asList(
                                new BooleanFieldTypeDetectionStrategy(),
                                new IbanFieldTypeDetectionStrategy(),
                                new SortCodeFieldTypeDetectionStrategy(),
                                new BbanFieldTypeDetectionStrategy(),
                                new IntegerFieldTypeDetectionStrategy(),
                                new DoubleFieldTypeDetectionStrategy(),
                                new DateFieldTypeDetectionStrategy(),
                                new MaskedPanFieldTypeDetectionStrategy(),
                                new StringFieldTypeDetectionStrategy(),
                                new DummyFieldTypeDetectionStrategy()))
                .build();
    }
}
