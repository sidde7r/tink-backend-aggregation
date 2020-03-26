package se.tink.backend.aggregation.agents.framework.assertions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.MapDifference;
import com.google.common.collect.MapDifference.ValueDifference;
import com.google.common.collect.Maps;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.framework.assertions.entities.DifferenceEntity;
import se.tink.backend.aggregation.agents.framework.assertions.entities.EmptyDifferenceEntity;
import se.tink.backend.aggregation.agents.framework.assertions.entities.ListDifferenceEntity;
import se.tink.backend.aggregation.agents.framework.assertions.entities.MapDifferenceEntity;
import se.tink.backend.aggregation.agents.models.Transaction;

public class AgentContractEntitiesAsserts {

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final Logger log = LoggerFactory.getLogger(AgentContractEntitiesAsserts.class);

    // We have to keep this just to make AmexV62UkMockServerAgentTest (where we create account and
    // transaction entities
    // by invoking the constructor methods of these entities) work, for other tests (where we read
    // these entities as maps
    // from a contract file, we do not need such list
    private static List<String> ignoredFields =
            Arrays.asList(
                    "exactBalance", "availableBalance", "exactAvailableCredit", "id", "accountId");

    public static <A, B> boolean areListsMatchingVerbose(List<A> expected, List<B> given) {
        DifferenceEntity difference = areListsMatching(expected, given);
        if (difference instanceof EmptyDifferenceEntity) {
            log.info("Lists are matching");
            return true;
        } else if (difference instanceof ListDifferenceEntity) {
            log.error("Size of the lists does not match!");
            return false;
        } else if (difference instanceof MapDifferenceEntity) {
            MapDifferenceEntity diff = (MapDifferenceEntity) difference;
            StringBuilder stringBuilder = new StringBuilder();

            stringBuilder.append(
                    "The following object in expected list could not be matched with anything in the given list\n");
            stringBuilder.append("Expected Object\n");
            stringBuilder.append(diff.getSerializedExpectedMap() + "\n");
            stringBuilder.append("The closest given object is the following\n");
            stringBuilder.append(diff.getSerializedGivenMap() + "\n");
            stringBuilder.append("The differences are the following:\n");
            if (diff.getEntriesOnlyOnExpected().size() > 0) {
                stringBuilder.append("The following keys only appear in expected object\n");
                diff.getEntriesOnlyOnExpected()
                        .keySet()
                        .forEach(key -> stringBuilder.append(key + "\n"));
            }
            if (diff.getDifferenceInCommonKeys().size() > 0) {
                stringBuilder.append(
                        "For the following keys the expected and given objects have different values\n");
                diff.getDifferenceInCommonKeys()
                        .keySet()
                        .forEach(key -> stringBuilder.append(key + "\n"));
            }
            log.error(stringBuilder.toString());
            return false;
        } else {
            throw new IllegalStateException("Difference type could not be handled");
        }
    }

    public static <A, B> DifferenceEntity areListsMatching(List<A> expected, List<B> given) {

        if (expected.size() != given.size()) {
            return new ListDifferenceEntity(expected.size(), given.size());
        }

        // To ensure that we are opearating on lists of maps
        List<Map<String, Object>> expectedCopy = convertToMapList(expected);
        List<Map<String, Object>> givenCopy = convertToMapList(given);

        for (Map<String, Object> expectedObject : expectedCopy) {
            DifferenceEntity closestMatch = findClosestMatch(expectedObject, givenCopy);
            // If the expected object could not match with any object in given list return the
            // closest match and the differences between them
            if (closestMatch instanceof MapDifferenceEntity) {
                return closestMatch;
            }
            // If there is a match for the expected object remove the matching given object from the
            // given list and continue
            else if (closestMatch instanceof EmptyDifferenceEntity) {
                givenCopy.remove(((EmptyDifferenceEntity) closestMatch).getGiven());
            } else {
                throw new IllegalStateException("Cannot handle the returned DifferenceEntity");
            }
        }

        return new EmptyDifferenceEntity(expected, given);
    }

    private static DifferenceEntity findClosestMatch(
            Map<String, Object> expectedObject, List<Map<String, Object>> givenList) {
        MapDifferenceEntity smallestDifference = null;
        for (Map<String, Object> givenObject : givenList) {
            MapDifferenceEntity difference = findDifferencesInMappings(expectedObject, givenObject);
            if (difference.getNumberOfDifferences() == 0) {
                return new EmptyDifferenceEntity(expectedObject, givenObject);
            } else if (smallestDifference == null
                    || smallestDifference.getNumberOfDifferences()
                            > difference.getNumberOfDifferences()) {
                smallestDifference = difference;
            }
        }
        return smallestDifference;
    }

    private static MapDifferenceEntity findDifferencesInMappings(
            Map<String, Object> expected, Map<String, Object> given) {

        MapDifference<String, Object> allDifferences = Maps.difference(expected, given);

        // If there is any key which appears only on expected the objects are not matching
        Map<String, Object> entriesOnlyOnLeft = allDifferences.entriesOnlyOnLeft();

        // Finds which values are different between expected and given maps for common keys
        Map<String, ValueDifference<Object>> differenceInCommonKeys =
                allDifferences.entriesDiffering();

        Map<String, ValueDifference<Object>> differences = new HashMap<>();
        differenceInCommonKeys.keySet().stream()
                .filter(key -> !ignoredFields.contains(key))
                .forEach(key -> differences.put(key, differenceInCommonKeys.get(key)));

        return new MapDifferenceEntity(entriesOnlyOnLeft, differences, expected, given);
    }

    private static <T> List<Map<String, Object>> convertToMapList(List<T> data) {
        List<Map<String, Object>> result = new ArrayList<>();
        data.forEach(
                entity -> {
                    try {
                        result.add(mapper.readValue(mapper.writeValueAsString(entity), Map.class));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
        return result;
    }

    private static <T> boolean areEqualIgnoringOrder(
            List<T> list1, List<T> list2, EqualityChecker comparator) {

        if (list1.size() != list2.size()) {
            return false;
        }

        List<T> copy1 = new ArrayList<>(list1);
        List<T> copy2 = new ArrayList<>(list2);

        for (T obj1 : copy1) {
            boolean found = false;
            for (T obj2 : copy2) {
                if (comparator.isEqual(obj1, obj2)) {
                    found = true;
                    copy2.remove(obj2);
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }

        return true;
    }

    public static void compareAccounts(List<Account> expected, List<Account> given) {
        Assert.assertTrue(
                areEqualIgnoringOrder(
                        expected,
                        given,
                        new EqualityChecker<Account>() {
                            @Override
                            public boolean isEqual(Account account1, Account account2) {
                                return Account.deepEquals(account1, account2);
                            }
                        }));
    }

    public static void compareTransactions(List<Transaction> expected, List<Transaction> given) {
        Assert.assertTrue(
                areEqualIgnoringOrder(
                        expected,
                        given,
                        new EqualityChecker<Transaction>() {
                            @Override
                            public boolean isEqual(
                                    Transaction transaction1, Transaction transaction2) {
                                return Double.compare(
                                                        transaction1.getAmount(),
                                                        transaction2.getAmount())
                                                == 0
                                        && Double.compare(
                                                        transaction1.getOriginalAmount(),
                                                        transaction2.getOriginalAmount())
                                                == 0
                                        && transaction1.isPending() == transaction2.isPending()
                                        && transaction1.isUpcoming() == transaction2.isUpcoming()
                                        && Objects.equals(
                                                transaction1.getCredentialsId(),
                                                transaction2.getCredentialsId())
                                        && Objects.equals(
                                                transaction1.getDate(), transaction2.getDate())
                                        && Objects.equals(
                                                transaction1.getDescription(),
                                                transaction2.getDescription())
                                        && transaction1.getType() == transaction2.getType()
                                        && Objects.equals(
                                                transaction1.getUserId(), transaction2.getUserId());
                            }
                        }));
    }
}
