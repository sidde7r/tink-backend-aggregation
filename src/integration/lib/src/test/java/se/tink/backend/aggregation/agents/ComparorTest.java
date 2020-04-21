package se.tink.backend.aggregation.agents;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.comparor.Comparor;
import se.tink.backend.aggregation.comparor.DifferenceCounter;
import se.tink.backend.aggregation.comparor.DifferenceEntity;
import se.tink.backend.aggregation.comparor.EmptyDifferenceEntity;
import se.tink.backend.aggregation.comparor.ListDifferenceEntity;
import se.tink.backend.aggregation.comparor.MapDifferenceEntity;

public class ComparorTest {

    private ObjectMapper mapper = new ObjectMapper();

    Comparor comparor =
            new Comparor(
                    new DifferenceCounter() {

                        private List<String> ignoredFields =
                                Arrays.asList(
                                        "exactBalance",
                                        "availableBalance",
                                        "exactAvailableCredit",
                                        "id",
                                        "accountId");

                        @Override
                        public int numberOfDifferences(MapDifferenceEntity allDifferences) {
                            return allDifferences.getEntriesOnlyOnExpected().size()
                                    + allDifferences.getDifferenceInCommonKeys().size();
                        }
                    });

    @Test
    public void shouldDetectFieldsThatOnlyAppearsInExpectedObject() throws IOException {
        // given
        String given = "[{\"field1\": \"value1\"}]";
        String expected = "[{\"field1\": \"value1\", \"field2\": \"value2\"}]";

        // when
        DifferenceEntity diff =
                comparor.areListsMatching(
                        mapper.readValue(expected, List.class),
                        mapper.readValue(given, List.class));
        // then
        Assert.assertTrue(diff instanceof MapDifferenceEntity);
        MapDifferenceEntity differences = (MapDifferenceEntity) diff;
        Assert.assertEquals(1, differences.getEntriesOnlyOnExpected().size());
    }

    @Test
    public void shouldDetectListWithDifferentSize() throws IOException {
        // given
        String given = "[]";
        String expected = "[{}]";
        // when
        DifferenceEntity diff =
                comparor.areListsMatching(
                        mapper.readValue(expected, List.class),
                        mapper.readValue(given, List.class));
        // then
        Assert.assertTrue(diff instanceof ListDifferenceEntity);
        ListDifferenceEntity differences = (ListDifferenceEntity) diff;
        Assert.assertEquals(1, differences.getExpectedListSize());
        Assert.assertEquals(0, differences.getGivenListSize());
    }

    @Test
    public void shouldDetectDifferencesInComplexObjects() throws IOException {
        // given
        String given = "[{\"object\": {\"field1\": \"value1\", \"field2\": \"value2\"} }]";
        String expected =
                "[{\"object\": {\"field1\": \"value1\", \"field2\": \"differentvalue\"} }]";
        // when
        DifferenceEntity diff =
                comparor.areListsMatching(
                        mapper.readValue(expected, List.class),
                        mapper.readValue(given, List.class));
        // then
        Assert.assertTrue(diff instanceof MapDifferenceEntity);
        MapDifferenceEntity differences = (MapDifferenceEntity) diff;
        Assert.assertEquals(1, differences.getDifferenceInCommonKeys().size());
    }

    @Test
    public void shouldDetectMatchingObjects() throws IOException {
        // given
        String given = "[{\"object\": {\"field2\": \"value2\", \"field1\": \"value1\"}}]";
        String expected = "[{\"object\": {\"field1\": \"value1\", \"field2\": \"value2\"}}]";
        // when
        DifferenceEntity diff =
                comparor.areListsMatching(
                        mapper.readValue(expected, List.class),
                        mapper.readValue(given, List.class));
        // then
        Assert.assertTrue(diff instanceof EmptyDifferenceEntity);
    }

    @Test
    public void shouldDetectMatchingLists() throws IOException {
        // given
        String given =
                "[{\"data\": {\"field1\": \"object1_value1\", \"field2\": \"object1_value2\"}}, {\"data\": {\"field1\": \"object2_value1\", \"field2\": \"object2_value2\"}}]";
        String expected =
                "[{\"data\": {\"field1\": \"object2_value1\", \"field2\": \"object2_value2\"}}, {\"data\": {\"field1\": \"object1_value1\", \"field2\": \"object1_value2\"}}]";
        // when
        DifferenceEntity diff =
                comparor.areListsMatching(
                        mapper.readValue(expected, List.class),
                        mapper.readValue(given, List.class));
        // then
        Assert.assertTrue(diff instanceof EmptyDifferenceEntity);
    }
}
