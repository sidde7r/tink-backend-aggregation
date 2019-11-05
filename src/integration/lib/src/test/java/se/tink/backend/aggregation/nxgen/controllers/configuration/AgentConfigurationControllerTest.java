package se.tink.backend.aggregation.nxgen.controllers.configuration;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;
import io.reactivex.rxjava3.disposables.Disposable;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.nxgen.controllers.configuration.configuration.NestedConfigurationLevel1;
import se.tink.backend.aggregation.nxgen.controllers.configuration.configuration.NestedConfigurationLevel2;
import se.tink.backend.aggregation.nxgen.controllers.configuration.configuration.OuterConfiguration;

public class AgentConfigurationControllerTest {
    private static final ObjectMapper OBJECT_MAPPER =
            new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private static String serializedConfiguration;
    private static Map<String, Object> serializedConfigurationAsMap;

    private AgentConfigurationController agentConfigurationController;

    @Before
    public void setup() {
        agentConfigurationController = new AgentConfigurationController();
    }

    @Test
    public void testNotifySecretValues() {
        Set<String> sensitiveValuesTestSet = new HashSet<>();
        Disposable disposable =
                agentConfigurationController
                        .getSecretValuesObservable()
                        .subscribe(
                                newSecretValues -> sensitiveValuesTestSet.addAll(newSecretValues));

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
                Sets.newHashSet("stringLevel1", "1", "stringLevel2", "2"),
                sensitiveValuesTestSet);

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
                Sets.newHashSet("stringLevel1", "1", "stringLevel2", "2", "stringLevel3", "3"),
                sensitiveValuesTestSet);
    }
}
