package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.fetcher.transactionalaccount.entities;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

import com.google.common.collect.ImmutableList;
import java.util.Arrays;
import java.util.List;

public enum BookedBalanceType {
    OPENING_BOOKED("openingBooked", 0),
    CLOSING_BOOKED("closingBooked", 1),
    EXPECTED("expected", 2),
    INTERIM_AVAILABLE("interimAvailable", 3);

    static final List<String> SUPPORTED_TYPES =
            Arrays.stream(BookedBalanceType.values())
                    .map(BookedBalanceType::getValue)
                    .collect(collectingAndThen(toList(), ImmutableList::copyOf));

    private final String value;
    private final int priority;

    BookedBalanceType(String value, int priority) {
        this.value = value;
        this.priority = priority;
    }

    public String getValue() {
        return value;
    }

    public int getPriority() {
        return priority;
    }
}
