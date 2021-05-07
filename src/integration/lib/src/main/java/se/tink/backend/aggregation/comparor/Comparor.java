package se.tink.backend.aggregation.comparor;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.MapDifference;
import com.google.common.collect.MapDifference.ValueDifference;
import com.google.common.collect.Maps;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Comparor {

    private static final String ACCOUNT_IDENTIFIERS_KEY = "identifiers";

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final Logger log = LoggerFactory.getLogger(Comparor.class);

    private DifferenceCounter differenceCounter;

    public Comparor(DifferenceCounter differenceCounter) {
        this.differenceCounter = differenceCounter;
    }

    public <A, B> DifferenceEntity areListsMatching(List<A> expected, List<B> given) {

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

    private DifferenceEntity findClosestMatch(
            Map<String, Object> expectedObject, List<Map<String, Object>> givenList) {
        DifferenceEntity smallestDifference = null;
        int smallest = Integer.MAX_VALUE;
        for (Map<String, Object> givenObject : givenList) {
            DifferenceEntity difference = findDifferencesInMappings(expectedObject, givenObject);
            if (difference instanceof EmptyDifferenceEntity) {
                return difference;
            } else if (smallestDifference == null
                    || smallest
                            > this.differenceCounter.numberOfDifferences(
                                    (MapDifferenceEntity) difference)) {
                smallestDifference = difference;
                smallest =
                        this.differenceCounter.numberOfDifferences(
                                (MapDifferenceEntity) difference);
            }
        }
        return smallestDifference;
    }

    public DifferenceEntity findDifferencesInMappings(
            Map<String, Object> expected, Map<String, Object> given) {

        MapDifference<String, Object> allDifferences = Maps.difference(expected, given);

        // If there is any key which appears only on expected the objects are not matching
        Map<String, Object> entriesOnlyOnLeft = allDifferences.entriesOnlyOnLeft();
        Map<String, Object> entriesOnlyOnRight = allDifferences.entriesOnlyOnRight();

        // Finds which values are different between expected and given maps for common keys
        Map<String, ValueDifference<Object>> differenceInCommonKeys =
                allDifferences.entriesDiffering();

        Map<String, ValueDifference<Object>> differences = new HashMap<>();
        differenceInCommonKeys.keySet().stream()
                .forEach(key -> differences.put(key, differenceInCommonKeys.get(key)));

        MapDifferenceEntity difference =
                new MapDifferenceEntity(
                        entriesOnlyOnLeft, entriesOnlyOnRight, differences, expected, given);

        if (this.differenceCounter.numberOfDifferences(difference) > 0) {
            return difference;
        } else {
            return new EmptyDifferenceEntity(expected, given);
        }
    }

    @SneakyThrows
    private <T> List<Map<String, Object>> convertToMapList(List<T> data) {
        List<Map<String, Object>> result = new ArrayList<>();
        data.forEach(
                entity -> {
                    try {
                        result.add(mapper.readValue(mapper.writeValueAsString(entity), Map.class));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });

        // This is a temporary solution to get account's identifiers in the expected order
        for (Map<String, Object> accountMap : result) {
            if (accountMap.containsKey(ACCOUNT_IDENTIFIERS_KEY)) {
                List<String> identifiers =
                        mapper.readValue(
                                accountMap.get(ACCOUNT_IDENTIFIERS_KEY).toString(),
                                new TypeReference<List<String>>() {});
                Collections.sort(identifiers);
                accountMap.put(ACCOUNT_IDENTIFIERS_KEY, mapper.writeValueAsString(identifiers));
            }
        }
        return result;
    }
}
