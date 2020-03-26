package se.tink.backend.aggregation.agents;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.assertions.AgentContractEntitiesAsserts;
import se.tink.backend.aggregation.agents.framework.assertions.entities.DifferenceEntity;
import se.tink.backend.aggregation.agents.framework.assertions.entities.EmptyDifferenceEntity;
import se.tink.backend.aggregation.agents.framework.assertions.entities.ListDifferenceEntity;
import se.tink.backend.aggregation.agents.framework.assertions.entities.MapDifferenceEntity;

public class ContractAsserterTest {

    private ObjectMapper mapper = new ObjectMapper();

    @Test
    public void shouldDetectFieldsThatOnlyAppearsInExpectedObject() throws IOException {
        // given
        String given = "[{\"field1\": \"value1\"}]";
        String expected = "[{\"field1\": \"value1\", \"field2\": \"value2\"}]";
        // when
        DifferenceEntity diff =
                AgentContractEntitiesAsserts.areListsMatching(
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
                AgentContractEntitiesAsserts.areListsMatching(
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
                AgentContractEntitiesAsserts.areListsMatching(
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
                AgentContractEntitiesAsserts.areListsMatching(
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
                AgentContractEntitiesAsserts.areListsMatching(
                        mapper.readValue(expected, List.class),
                        mapper.readValue(given, List.class));
        // then
        Assert.assertTrue(diff instanceof EmptyDifferenceEntity);
    }
}
