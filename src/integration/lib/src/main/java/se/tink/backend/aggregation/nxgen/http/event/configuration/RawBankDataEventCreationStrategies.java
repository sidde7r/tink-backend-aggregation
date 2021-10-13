package se.tink.backend.aggregation.nxgen.http.event.configuration;

import java.util.Collections;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import se.tink.backend.aggregation.nxgen.http.event.decision_strategy.AllowAlwaysRawBankDataEventCreationTriggerStrategy;
import se.tink.backend.aggregation.nxgen.http.event.decision_strategy.RawBankDataEventCreationTriggerStrategy;
import se.tink.backend.aggregation.nxgen.http.event.masking.keys.MaskKeysWithNumericValuesStrategy;
import se.tink.backend.aggregation.nxgen.http.event.masking.keys.RawBankDataKeyValueMaskingStrategy;
import se.tink.backend.aggregation.nxgen.http.event.masking.values.MaskAllValueMaskingStrategy;
import se.tink.backend.aggregation.nxgen.http.event.masking.values.RawBankDataFieldValueMaskingStrategy;
import se.tink.backend.aggregation.nxgen.http.event.type_detection.DummyFieldTypeDetectionStrategy;
import se.tink.backend.aggregation.nxgen.http.event.type_detection.RawBankDataFieldTypeDetectionStrategy;

@Builder
@Getter
public class RawBankDataEventCreationStrategies {

    private final List<RawBankDataKeyValueMaskingStrategy> keyMaskingStrategies;
    private final List<RawBankDataFieldValueMaskingStrategy> valueMaskingStrategies;
    private final List<RawBankDataFieldTypeDetectionStrategy> fieldTypeDetectionStrategies;
    private final RawBankDataEventCreationTriggerStrategy emissionDecisionStrategy;

    public static RawBankDataEventCreationStrategies allowEmissionWithDefaultSettings() {
        return RawBankDataEventCreationStrategies.builder()
                .keyMaskingStrategies(
                        Collections.singletonList(new MaskKeysWithNumericValuesStrategy()))
                .valueMaskingStrategies(
                        Collections.singletonList(new MaskAllValueMaskingStrategy()))
                .fieldTypeDetectionStrategies(
                        Collections.singletonList(new DummyFieldTypeDetectionStrategy()))
                .emissionDecisionStrategy(new AllowAlwaysRawBankDataEventCreationTriggerStrategy())
                .build();
    }
}
