package se.tink.backend.aggregation.agents.framework.assertions;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.comparor.DifferenceCounter;
import se.tink.backend.aggregation.comparor.MapDifferenceEntity;

public class ContractEntityDifferenceCounter implements DifferenceCounter {

    /*
    We have to keep this just to make AmexV62UkMockServerAgentTest (where we create account and
    transaction entities by invoking the constructor methods of these entities) work, for
    other tests (where we read these entities as maps from a contract file, we do not need such list
     */
    private List<String> ignoredFields =
            Arrays.asList(
                    "exactBalance", "availableBalance", "exactAvailableCredit", "id", "accountId");

    private int getNumberOfKeysWithoutIgnoredFields(Set<String> keys) {
        return keys.stream()
                .filter(key -> !ignoredFields.contains(key))
                .collect(Collectors.toSet())
                .size();
    }

    @Override
    public int numberOfDifferences(MapDifferenceEntity allDifferences) {

        return getNumberOfKeysWithoutIgnoredFields(
                        allDifferences.getEntriesOnlyOnExpected().keySet())
                + getNumberOfKeysWithoutIgnoredFields(
                        allDifferences.getDifferenceInCommonKeys().keySet());
    }
}
