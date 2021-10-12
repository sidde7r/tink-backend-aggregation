package se.tink.backend.aggregation.nxgen.http.event.configuration;

import java.util.Collections;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import se.tink.backend.aggregation.nxgen.http.event.decision_strategy.AllowAlwaysRawBankDataEventEmissionDecisionStrategy;
import se.tink.backend.aggregation.nxgen.http.event.decision_strategy.RawBankDataEventEmissionDecisionStrategy;
import se.tink.backend.aggregation.nxgen.http.event.masking.keys.MaskKeysWithNumericValuesStrategy;
import se.tink.backend.aggregation.nxgen.http.event.masking.keys.RawBankDataKeyValueMaskingStrategy;
import se.tink.backend.aggregation.nxgen.http.event.masking.values.MaskAllValueMaskingStrategy;
import se.tink.backend.aggregation.nxgen.http.event.masking.values.RawBankDataFieldValueMaskingStrategy;
import se.tink.backend.aggregation.nxgen.http.event.type_detection.DummyFieldTypeDetectionStrategy;
import se.tink.backend.aggregation.nxgen.http.event.type_detection.RawBankDataFieldTypeDetectionStrategy;

@Builder
@Getter
public class RawBankDataEventEmissionConfiguration {

    private final List<RawBankDataKeyValueMaskingStrategy> keyMaskingStrategies;
    private final List<RawBankDataFieldValueMaskingStrategy> valueMaskingStrategies;
    private final List<RawBankDataFieldTypeDetectionStrategy> fieldTypeDetectionStrategies;
    private final RawBankDataEventEmissionDecisionStrategy emissionDecisionStrategy;

    public static RawBankDataEventEmissionConfiguration allowEmissionWithDefaultSettings() {
        return RawBankDataEventEmissionConfiguration.builder()
                .keyMaskingStrategies(
                        Collections.singletonList(new MaskKeysWithNumericValuesStrategy()))
                .valueMaskingStrategies(
                        Collections.singletonList(new MaskAllValueMaskingStrategy()))
                .fieldTypeDetectionStrategies(
                        Collections.singletonList(new DummyFieldTypeDetectionStrategy()))
                .emissionDecisionStrategy(new AllowAlwaysRawBankDataEventEmissionDecisionStrategy())
                .build();
    }
}
