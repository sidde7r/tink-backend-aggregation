package se.tink.backend.aggregation.nxgen.controllers.configuration;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.nxgen.controllers.configuration.configuration.NestedConfigurationLevel1;
import se.tink.backend.aggregation.nxgen.controllers.configuration.configuration.NestedConfigurationLevel2;
import se.tink.backend.aggregation.nxgen.controllers.configuration.configuration.OuterConfiguration;

public class AgentConfigurationControllerExtractSensitiveValuesTest {

    private static final ObjectMapper OBJECT_MAPPER =
            new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private static String serializedConfiguration;
    private static Map<String, Object> serializedConfigurationAsMap;
    private AgentConfigurationController agentConfigurationController;

    @Before
    public void setUp() {
        agentConfigurationController = new AgentConfigurationController();
    }

    @Test
    public void testExtractSensitiveValues() {
        NestedConfigurationLevel2 nestedConfigurationLevel2 =
                new NestedConfigurationLevel2("stringLevel3", 3);
        NestedConfigurationLevel1 nestedConfigurationLevel1 =
                new NestedConfigurationLevel1("stringLevel2", 2, nestedConfigurationLevel2);
        OuterConfiguration outerConfiguration =
                new OuterConfiguration("stringLevel1", 1, nestedConfigurationLevel1);

        try {
            serializedConfiguration = OBJECT_MAPPER.writeValueAsString(outerConfiguration);
            serializedConfigurationAsMap =
                    OBJECT_MAPPER.readValue(serializedConfiguration, Map.class);
        } catch (IOException e) {
            Assert.fail("Error when serializing test configuration.");
        }

        Set<String> extractedSensitiveValues =
                agentConfigurationController.extractSensitiveValues(serializedConfigurationAsMap);

        Assert.assertTrue(
                "Extracted values are not what was expected.",
                extractedSensitiveValues.containsAll(
                        Sets.newHashSet(
                                "stringLevel1", "1", "stringLevel2", "2", "stringLevel3", "3")));
    }

    @Test
    public void testExtractSensitiveValuesWithList() {
        List<NestedConfigurationLevel2> listNestedConfigurationLevel2bis =
                IntStream.range(1, 3)
                        .mapToObj(
                                i -> new NestedConfigurationLevel2("stringLevel" + (6 + i), 6 + i))
                        .collect(Collectors.toList());
        NestedConfigurationLevel2 nestedConfigurationLevel2bis =
                new NestedConfigurationLevel2("stringLevel4", 4, listNestedConfigurationLevel2bis);
        List<NestedConfigurationLevel2> listNestedConfigurationLevel2 =
                IntStream.range(1, 3)
                        .mapToObj(
                                i -> new NestedConfigurationLevel2("stringLevel" + (4 + i), 4 + i))
                        .collect(Collectors.toList());
        NestedConfigurationLevel2 nestedConfigurationLevel2 =
                new NestedConfigurationLevel2("stringLevel3", 3, listNestedConfigurationLevel2);
        nestedConfigurationLevel2.setNestedConfigurationLevel2(nestedConfigurationLevel2bis);
        NestedConfigurationLevel1 nestedConfigurationLevel1 =
                new NestedConfigurationLevel1("stringLevel2", 2, nestedConfigurationLevel2);
        OuterConfiguration outerConfiguration =
                new OuterConfiguration("stringLevel1", 1, nestedConfigurationLevel1);

        try {
            serializedConfiguration = OBJECT_MAPPER.writeValueAsString(outerConfiguration);
            serializedConfigurationAsMap =
                    OBJECT_MAPPER.readValue(serializedConfiguration, Map.class);
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail("Error when serializing test configuration.");
        }

        Set<String> extractedSensitiveValues =
                agentConfigurationController.extractSensitiveValues(serializedConfigurationAsMap);

        Assert.assertTrue(
                "Extracted values are not what was expected.",
                extractedSensitiveValues.containsAll(
                        Sets.newHashSet(
                                "stringLevel1",
                                "1",
                                "stringLevel2",
                                "2",
                                "stringLevel3",
                                "3",
                                "stringLevel4",
                                "4",
                                "stringLevel5",
                                "5",
                                "stringLevel6",
                                "6",
                                "stringLevel7",
                                "7")));
    }

    @Test(expected = IllegalStateException.class)
    public void testExtractSensitiveValuesMaximumRecursionLevelReached() {
        NestedConfigurationLevel2 nestedConfigurationLevel2 =
                new NestedConfigurationLevel2("stringLevel3", 3);
        NestedConfigurationLevel2 nestedConfigurationLevel2Recursive = nestedConfigurationLevel2;
        for (int i = 0;
                i < AgentConfigurationController.MAX_RECURSION_DEPTH_EXTRACT_SENSITIVE_VALUES;
                ++i) {
            NestedConfigurationLevel2 nestedConfigurationLevel2OneMoreLevel =
                    new NestedConfigurationLevel2("stringLevel" + (4 + i), 4 + i);
            nestedConfigurationLevel2Recursive.setNestedConfigurationLevel2(
                    nestedConfigurationLevel2OneMoreLevel);
            nestedConfigurationLevel2Recursive = nestedConfigurationLevel2OneMoreLevel;
        }

        NestedConfigurationLevel1 nestedConfigurationLevel1 =
                new NestedConfigurationLevel1("stringLevel2", 2, nestedConfigurationLevel2);
        OuterConfiguration outerConfiguration =
                new OuterConfiguration("stringLevel1", 1, nestedConfigurationLevel1);

        try {
            serializedConfiguration = OBJECT_MAPPER.writeValueAsString(outerConfiguration);
            serializedConfigurationAsMap =
                    OBJECT_MAPPER.readValue(serializedConfiguration, Map.class);
        } catch (IOException e) {
            Assert.fail("Error when serializing test configuration.");
        }

        agentConfigurationController.extractSensitiveValues(serializedConfigurationAsMap);
    }
}
