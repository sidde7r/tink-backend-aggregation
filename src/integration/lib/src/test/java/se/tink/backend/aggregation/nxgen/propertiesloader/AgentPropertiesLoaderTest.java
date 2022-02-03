package se.tink.backend.aggregation.nxgen.propertiesloader;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.FileNotFoundException;
import java.io.IOException;
import org.junit.Test;

public class AgentPropertiesLoaderTest {

    private static final String PROPERTIES_LOADER_PARENT_PATH =
            "src/integration/lib/src/test/java/se/tink/backend/aggregation/nxgen";

    private AgentPropertiesLoader propertiesLoader;

    @Test
    public void shouldLoadPropertiesFromDefaultLocalisation() throws IOException {
        // given
        propertiesLoader =
                new AgentPropertiesLoader(AgentPropertiesFixtures.propertiesLoaderPath());

        // when
        AgentPropertiesTestEntity agentPropertiesTestEntity =
                propertiesLoader.load(AgentPropertiesTestEntity.class);

        // then
        AgentPropertiesFixtures.assertPropertiesAreEqualToExpectedValues(agentPropertiesTestEntity);
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
        propertiesLoader =
                new AgentPropertiesLoader(AgentPropertiesFixtures.propertiesLoaderPath());

        // expect
        assertThatThrownBy(() -> propertiesLoader.load(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    public void shouldThrowWhenPropertiesFileIsNotFound() {
        // given
        propertiesLoader = new AgentPropertiesLoader(PROPERTIES_LOADER_PARENT_PATH);

        // expect
        assertThatThrownBy(() -> propertiesLoader.load(AgentPropertiesTestEntity.class))
                .isInstanceOf(FileNotFoundException.class);
    }
}
