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

    PREVIOUSLY_CLOSED_BOOKED("previouslyClosedBooked", "PRCD"),
    CLOSING_BOOKED("closingBooked", "CLBD"),
    CLOSING_AVAILABLE("closingAvailable", "CLAV"),
    EXPECTED("expected", "XPCD"),
    OPENING_BOOKED("openingBooked", "OPBD"),
    INTERIM_AVAILABLE("interimAvailable", "ITAV"),
    INTERIM_BOOKED("interimBooked", "ITBD"),
    FORWARD_AVAILABLE("forwardAvailable", "FWAV"),
    NON_INVOICED("nonInvoiced", null);
    private String type;
    private String shortType;

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
