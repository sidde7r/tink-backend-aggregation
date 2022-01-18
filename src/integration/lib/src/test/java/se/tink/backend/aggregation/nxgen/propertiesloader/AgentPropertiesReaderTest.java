package se.tink.backend.aggregation.nxgen.propertiesloader;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AgentPropertiesReaderTest {

    private static final String RESOURCE_PATH =
            "src/integration/lib/src/test/java/se/tink/backend/aggregation/nxgen/propertiesloader/resources/";

    @Test
    public void shouldReadPropertiesFromYamlFile() throws IOException {
        // given:
        AgentPropertiesReader agentPropertiesReader = new AgentPropertiesReader();
        File propertiesFile = new File(RESOURCE_PATH + "agent-test-properties.yaml");

        // when:
        AgentPropertiesTestEntity agentPropertiesTestEntity =
                agentPropertiesReader.read(propertiesFile, AgentPropertiesTestEntity.class);

        // then:
        assertThat(agentPropertiesTestEntity.getApiVersion()).isEqualTo("v3");
        assertThat(agentPropertiesTestEntity.getEnv()).isEqualTo("prod");
        assertThat(agentPropertiesTestEntity.getCertainDate())
                .isEqualTo(LocalDateTime.of(2019, 4, 17, 10, 15, 30));
        assertThat(agentPropertiesTestEntity.getUrlList().size()).isEqualTo(2);
    }
}
