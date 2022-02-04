package se.tink.backend.aggregation.nxgen.propertiesloader;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static se.tink.backend.aggregation.nxgen.propertiesloader.AgentPropertiesFixtures.PROPERTIES_LOADER_PATH;
import static se.tink.backend.aggregation.nxgen.propertiesloader.AgentPropertiesFixtures.PROPERTIES_RESOURCE_PATH;
import static se.tink.backend.aggregation.nxgen.propertiesloader.AgentPropertiesFixtures.assertPropertiesAreEqualToExpectedValues;

import java.io.IOException;
import org.junit.Test;

public class AgentPropertiesLoaderTest {

    private static final String PROPERTIES_LOADER_PARENT_PATH =
            "src/integration/lib/src/test/java/se/tink/backend/aggregation/nxgen";

    private AgentPropertiesLoader agentPropertiesLoader;

    @Test
    public void shouldLoadPropertiesFromDefaultLocalisation() throws IOException {
        // given
        agentPropertiesLoader = new AgentPropertiesLoader(PROPERTIES_LOADER_PATH);

        // when
        AgentPropertiesTestEntity agentPropertiesTestEntity =
                agentPropertiesLoader.load(AgentPropertiesTestEntity.class);

        // then
        assertPropertiesAreEqualToExpectedValues(agentPropertiesTestEntity);
    }

    @Test
    public void shouldThrowWhenClassPathIsNull() {
        // expect
        assertThatThrownBy(() -> new AgentPropertiesLoader(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    public void shouldThrowWhenPropertiesClassIsNull() {
        // given
        agentPropertiesLoader = new AgentPropertiesLoader(PROPERTIES_LOADER_PATH);

        // expect
        assertThatThrownBy(() -> agentPropertiesLoader.load(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    public void shouldThrowWhenPropertiesFileIsNotFound() {
        // given
        agentPropertiesLoader = new AgentPropertiesLoader(PROPERTIES_LOADER_PARENT_PATH);

        // expect
        assertThatThrownBy(() -> agentPropertiesLoader.load(AgentPropertiesTestEntity.class))
                .isInstanceOf(AgentPropertiesLoaderException.class)
                .hasMessageContaining("Couldn't find properties file");
    }

    @Test
    public void shouldThrowWhenMoreThanOneFileWithGivenSuffixIsFound() {
        // given
        agentPropertiesLoader = new AgentPropertiesLoader(PROPERTIES_RESOURCE_PATH);

        // expect
        assertThatThrownBy(() -> agentPropertiesLoader.load(AgentPropertiesTestEntity.class))
                .isInstanceOf(AgentPropertiesLoaderException.class)
                .hasMessageContaining("Found more than one file");
    }
}
