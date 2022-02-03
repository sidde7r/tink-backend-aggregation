package se.tink.backend.aggregation.nxgen.propertiesloader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.File;
import java.util.Optional;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JUnitParamsRunner.class)
public class AgentPropertiesScannerTest {

    private static final String PROPERTIES_TEST_SUFFIX = "-test-prod-properties.yaml";

    private final AgentPropertiesScanner scanner = new AgentPropertiesScanner();

    @Test
    public void shouldFindPropertiesFileInGivenPackage() {
        // when
        Optional<File> propertiesFile =
                scanner.scan(AgentPropertiesFixtures.resourcesPath(), PROPERTIES_TEST_SUFFIX);

        // then
        assertThat(propertiesFile).isPresent();
    }

    @Test
    public void shouldThrowNPEWhenPackageIsNull() {
        // expect
        assertThatThrownBy(() -> scanner.scan(null, PROPERTIES_TEST_SUFFIX))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @Parameters
    public void shouldReturnOptionalEmptyWhenPropertiesFileIsNotFound(String path, String suffix) {
        // when
        Optional<File> propertiesFile = scanner.scan(path, suffix);

        // then
        assertThat(propertiesFile).isEmpty();
    }

    @SuppressWarnings("unused")
    private Object[] parametersForShouldReturnOptionalEmptyWhenPropertiesFileIsNotFound() {
        return new Object[][] {
            {AgentPropertiesFixtures.propertiesLoaderPath(), PROPERTIES_TEST_SUFFIX},
            {AgentPropertiesFixtures.resourcesPath(), "nonexistent-file-suffix"}
        };
    }
}
