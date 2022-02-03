package se.tink.backend.aggregation.nxgen.propertiesloader;

import java.io.File;
import java.io.IOException;
import org.junit.Test;

public class AgentPropertiesReaderTest {

    @Test
    public void shouldReadPropertiesFromYamlFile() throws IOException {
        // given:
        AgentPropertiesReader agentPropertiesReader = new AgentPropertiesReader();
        File propertiesFile =
                new File(
                        AgentPropertiesFixtures.resourcesPath()
                                + "agent-test-prod-properties.yaml");

        // when:
        AgentPropertiesTestEntity agentPropertiesTestEntity =
                agentPropertiesReader.read(propertiesFile, AgentPropertiesTestEntity.class);

        // then:
        AgentPropertiesFixtures.assertPropertiesAreEqualToExpectedValues(agentPropertiesTestEntity);
    }
}
