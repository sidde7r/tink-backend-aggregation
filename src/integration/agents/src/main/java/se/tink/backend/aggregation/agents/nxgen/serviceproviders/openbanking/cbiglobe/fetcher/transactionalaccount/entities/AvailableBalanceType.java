package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.fetcher.transactionalaccount.entities;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

import com.google.common.collect.ImmutableList;
import java.util.Arrays;
import java.util.List;

public enum AvailableBalanceType {
    INTERIM_AVAILABLE("interimAvailable", 0),
    EXPECTED("expected", 1),
    FORWARD_AVAILABLE("forwardAvailable", 2);

    static final List<String> SUPPORTED_TYPES =
            Arrays.stream(AvailableBalanceType.values())
                    .map(AvailableBalanceType::getValue)
                    .collect(collectingAndThen(toList(), ImmutableList::copyOf));

    private final String value;
    private final int priority;

    AvailableBalanceType(String value, int priority) {
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
