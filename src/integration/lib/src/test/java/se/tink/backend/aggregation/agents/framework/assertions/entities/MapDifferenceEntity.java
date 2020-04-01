package se.tink.backend.aggregation.agents.framework.assertions.entities;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.MapDifference.ValueDifference;
import java.util.Map;

public class MapDifferenceEntity implements DifferenceEntity {

    private final Map<String, Object> entriesOnlyOnExpected;
    private Map<String, ValueDifference<Object>> differenceInCommonKeys;
    private Map<String, Object> expectedMap;
    private Map<String, Object> givenMap;
    private static final ObjectMapper mapper = new ObjectMapper();

    public MapDifferenceEntity(
            Map<String, Object> entriesOnlyOnExpected,
            Map<String, ValueDifference<Object>> differenceInCommonKeys,
            Map<String, Object> expectedMap,
            Map<String, Object> givenMap) {
        this.entriesOnlyOnExpected = entriesOnlyOnExpected;
        this.differenceInCommonKeys = differenceInCommonKeys;
        this.expectedMap = expectedMap;
        this.givenMap = givenMap;
    }

    public int getNumberOfDifferences() {
        return this.entriesOnlyOnExpected.size() + this.differenceInCommonKeys.size();
    }

    public Map<String, Object> getEntriesOnlyOnExpected() {
        return entriesOnlyOnExpected;
    }

    public Map<String, ValueDifference<Object>> getDifferenceInCommonKeys() {
        return differenceInCommonKeys;
    }

    public Map<String, Object> getExpectedMap() {
        return expectedMap;
    }

    public Map<String, Object> getGivenMap() {
        return givenMap;
    }

    private String serialize(Map<String, Object> map) {
        try {
            return mapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public String getSerializedExpectedMap() {
        return serialize(expectedMap);
    }

    public String getSerializedGivenMap() {
        return serialize(givenMap);
    }
}
