package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.payment.enums;

import java.util.stream.Stream;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import se.tink.libraries.transfer.rpc.Frequency;

@Getter
@RequiredArgsConstructor
public enum IngPaymentFrequency {
    DAILY("DAIL", Frequency.DAILY),
    WEEKLY("WEEK", Frequency.WEEKLY),
    EVERY_TWO_WEEKS("TOWK", Frequency.EVERY_TWO_WEEKS),
    MONTHLY("MNTH", Frequency.MONTHLY),
    EVERY_TWO_MONTHS("TOMN", Frequency.EVERY_TWO_MONTHS),
    QUARTERLY("QUTR", Frequency.QUARTERLY),
    SEMI_ANNUAL("SEMI", Frequency.SEMI_ANNUAL),
    ANNUAL("YEAR", Frequency.ANNUAL);

    private final String apiValue;
    private final Frequency tinkFrequency;

    public static IngPaymentFrequency getForTinkFrequency(Frequency frequency) {
        return Stream.of(IngPaymentFrequency.values())
                .filter(ingFrequency -> ingFrequency.getTinkFrequency() == frequency)
                .findFirst()
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        "[ING] Cannot match frequency for: " + frequency));
    }
}
