package se.tink.backend.aggregation.nxgen.propertiesloader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static se.tink.backend.aggregation.nxgen.propertiesloader.AgentPropertiesFixtures.PROPERTIES_LOADER_PATH;
import static se.tink.backend.aggregation.nxgen.propertiesloader.AgentPropertiesFixtures.PROPERTIES_RESOURCE_PATH;

import java.io.File;
import java.util.List;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JUnitParamsRunner.class)
public class AgentPropertiesScannerTest {

    private static final String PROPERTIES_TEST_SUFFIX = "-agent-prod.yaml";

    private final AgentPropertiesScanner agentPropertiesScanner = new AgentPropertiesScanner();

    @Test
    public void shouldFindPropertiesFileInGivenPackage() {
        // when
        List<File> propertiesFile =
                agentPropertiesScanner.scan(PROPERTIES_RESOURCE_PATH, PROPERTIES_TEST_SUFFIX);

        // then
        assertThat(propertiesFile).isNotEmpty();
    }

    @Test
    public void shouldThrowNPEWhenPackageIsNull() {
        // expect
        assertThatThrownBy(() -> agentPropertiesScanner.scan(null, PROPERTIES_TEST_SUFFIX))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @Parameters
    public void shouldReturnEmptyListWhenPropertiesFileIsNotFound(String path, String suffix) {
        // when
        List<File> propertiesFile = agentPropertiesScanner.scan(path, suffix);

        // then
        assertThat(propertiesFile).isEmpty();
    }

    @SuppressWarnings("unused")
    private Object[] parametersForShouldReturnOptionalEmptyWhenPropertiesFileIsNotFound() {
        return new Object[][] {
            {PROPERTIES_LOADER_PATH, PROPERTIES_TEST_SUFFIX},
            {PROPERTIES_RESOURCE_PATH, "nonexistent-file-suffix"}
        };
    }
}
