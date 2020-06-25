package se.tink.backend.aggregation.agents.utils.berlingroup;

import java.util.Arrays;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum BalanceType {
    // Values extracted from BerlinGroup specification, version 1.3.6

    CLOSING_BOOKED("closingBooked"),
    EXPECTED("expected"),
    OPENING_BOOKED("openingBooked"),
    INTERIM_AVAILABLE("interimAvailable"),
    INTERIM_BOOKED("interimBooked"),
    FORWARD_AVAILABLE("forwardAvailable"),
    NON_INVOICED("nonInvoiced");
    private String type;

    private static BalanceType[] values = BalanceType.values();

    public static Optional<BalanceType> findByStringType(String balanceType) {
        return Arrays.stream(values).filter(x -> x.type.equalsIgnoreCase(balanceType)).findAny();
    }

    public static boolean isKnownType(String balanceType) {
        return findByStringType(balanceType).isPresent();
    }
}
