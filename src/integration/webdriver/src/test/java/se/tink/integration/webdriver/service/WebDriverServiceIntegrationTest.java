package se.tink.integration.webdriver.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import lombok.SneakyThrows;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.aggregation.nxgen.storage.AgentTemporaryStorage;

@RunWith(JUnitParamsRunner.class)
public class WebDriverServiceIntegrationTest {

    private static WebDriverService driverService;

    private static final String TEST_DATA_DIR =
            "src/integration/webdriver/src/test/java/se/tink/integration/webdriver/service/resources/page_source";

    @BeforeClass
    public static void setup() {
        driverService =
                WebDriverServiceModule.createWebDriverService(mock(AgentTemporaryStorage.class));
    }

    @AfterClass
    public static void cleanup() {
        driverService.terminate(mock(AgentTemporaryStorage.class));
    }

    @Test
    @Parameters(method = "paramsForCorrectPageSourceLog")
    public void should_create_correct_page_source_log(int iframeLevel, String expectedLog) {
        // given
        driverService.get(getUriToHtmlFile("top_page.html"));

        // when
        String log = driverService.getFullPageSourceLog(iframeLevel);

        // then
        assertThat(log).isEqualTo(expectedLog);
    }

    @SuppressWarnings("unused")
    private static Object[] paramsForCorrectPageSourceLog() {
        return new Object[] {
            new Object[] {0, readExpectedLogFile("expected_log_lvl_0.html")},
            new Object[] {1, readExpectedLogFile("expected_log_lvl_1.html")},
            new Object[] {2, readExpectedLogFile("expected_log_lvl_2.html")},
            new Object[] {3, readExpectedLogFile("expected_log_lvl_3.html")},
            new Object[] {4, readExpectedLogFile("expected_log_lvl_3.html")},
            new Object[] {5, readExpectedLogFile("expected_log_lvl_3.html")}
        };
    }

    @SneakyThrows
    private static String readExpectedLogFile(String fileName) {
        Path filePath = Paths.get(TEST_DATA_DIR, fileName);
        return new String(Files.readAllBytes(filePath));
    }

    @SuppressWarnings("SameParameterValue")
    private String getUriToHtmlFile(String fileName) {
        return Paths.get(TEST_DATA_DIR, fileName).toUri().toString();
    }
}
