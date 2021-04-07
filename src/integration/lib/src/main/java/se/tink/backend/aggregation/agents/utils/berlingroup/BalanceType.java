package se.tink.backend.aggregation.agents.utils.berlingroup;

import java.util.Arrays;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
@Getter
public enum BalanceType {
    // Values extracted from BerlinGroup specification, version 1.3.6

    PREVIOUSLY_CLOSED_BOOKED("previouslyClosedBooked"),
    CLOSING_BOOKED("closingBooked"),
    CLOSING_AVAILABLE("closingAvailable"),
    EXPECTED("expected"),
    OPENING_BOOKED("openingBooked"),
    INTERIM_AVAILABLE("interimAvailable"),
    INTERIM_BOOKED("interimBooked"),
    FORWARD_AVAILABLE("forwardAvailable"),
    NON_INVOICED("nonInvoiced");
    private String type;

    private static BalanceType[] values = BalanceType.values();

    public static Optional<BalanceType> findByStringType(String balanceType) {
        Optional<BalanceType> balType =
                Arrays.stream(values).filter(x -> x.type.equalsIgnoreCase(balanceType)).findAny();
        if (!balType.isPresent()) {
            log.warn("Found balance entities with unknown balance type: " + balanceType);
        }
        return balType;
    }
}
