package se.tink.backend.aggregation.nxgen.controllers.configuration;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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

        List<String> extractedSensitiveValues =
                agentConfigurationController.extractSensitiveValues(serializedConfigurationAsMap);

        Assert.assertEquals(
                "Extracted values are not what was expected.",
                Arrays.asList("stringLevel1", "1", "stringLevel2", "2", "stringLevel3", "3"),
                extractedSensitiveValues);
    }

    @Test
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

        try {
            agentConfigurationController.extractSensitiveValues(serializedConfigurationAsMap);
        } catch (IllegalStateException e) {
            Assert.assertEquals(
                    "Unexpected error message or IllegalStateException when trying to test maximum recursion level to extract sensitive values.",
                    "Reached maximum recursion depth when trying to extract sensitive configuration values.",
                    e.getMessage());
        } catch (Exception e) {
            Assert.fail(
                    "Unexpected Exception when trying to test maximum recursion level to extract sensitive values : "
                            + e);
        }
    }

    @Test
    public void testNotifySecretValues() {
        class TestPropertyChangeListener implements PropertyChangeListener {
            public List<String> sensitiveValues = Collections.emptyList();

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                sensitiveValues = (List<String>) evt.getNewValue();
            }
        }

        TestPropertyChangeListener testPropertyChangeListener = new TestPropertyChangeListener();
        agentConfigurationController.addObserver(testPropertyChangeListener);

        NestedConfigurationLevel1 nestedConfigurationLevel1 =
                new NestedConfigurationLevel1("stringLevel2", 2, null);
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

        Assert.assertEquals(
                "Extracted values are not what was expected.",
                Arrays.asList("stringLevel1", "1", "stringLevel2", "2"),
                testPropertyChangeListener.sensitiveValues);

        NestedConfigurationLevel2 nestedConfigurationLevel2 =
                new NestedConfigurationLevel2("stringLevel3", 3);
        nestedConfigurationLevel1.setNestedConfigurationLevel2(nestedConfigurationLevel2);

        try {
            serializedConfiguration = OBJECT_MAPPER.writeValueAsString(outerConfiguration);
            serializedConfigurationAsMap =
                    OBJECT_MAPPER.readValue(serializedConfiguration, Map.class);
        } catch (IOException e) {
            Assert.fail("Error when serializing test configuration.");
        }

        agentConfigurationController.extractSensitiveValues(serializedConfigurationAsMap);

        Assert.assertEquals(
                "Extracted values are not what was expected.",
                Arrays.asList("stringLevel1", "1", "stringLevel2", "2", "stringLevel3", "3"),
                testPropertyChangeListener.sensitiveValues);
    }
}
