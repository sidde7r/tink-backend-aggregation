package se.tink.backend.aggregation.agents.framework.assertions;

import java.util.Set;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.comparor.DifferenceCounter;
import se.tink.backend.aggregation.comparor.MapDifferenceEntity;

public class ContractEntityDifferenceCounter implements DifferenceCounter {

    private int getNumberOfKeysWithoutIgnoredFields(Set<String> keys) {
        return keys.stream().collect(Collectors.toSet()).size();
    }

    @Override
    public int numberOfDifferences(MapDifferenceEntity allDifferences) {

        return allDifferences.getEntriesOnlyOnExpected().keySet().size()
                + allDifferences.getDifferenceInCommonKeys().keySet().size();
    }
}
