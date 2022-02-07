package se.tink.backend.aggregation.nxgen.propertiesloader;

import static se.tink.backend.aggregation.nxgen.propertiesloader.AgentPropertiesFixtures.PROPERTIES_RESOURCE_PATH;
import static se.tink.backend.aggregation.nxgen.propertiesloader.AgentPropertiesFixtures.assertPropertiesAreEqualToExpectedValues;

import java.io.File;
import java.io.IOException;
import org.junit.Test;

public class AgentPropertiesReaderTest {

    @Test
    public void shouldReadPropertiesFromYamlFile() throws IOException {
        // given:
        AgentPropertiesReader agentPropertiesReader = new AgentPropertiesReader();
        File propertiesFile = new File(PROPERTIES_RESOURCE_PATH + "/test-agent-prod.yaml");

        // when:
        AgentPropertiesTestEntity agentPropertiesTestEntity =
                agentPropertiesReader.read(propertiesFile, AgentPropertiesTestEntity.class);

        // then:
        assertPropertiesAreEqualToExpectedValues(agentPropertiesTestEntity);
    }
}
