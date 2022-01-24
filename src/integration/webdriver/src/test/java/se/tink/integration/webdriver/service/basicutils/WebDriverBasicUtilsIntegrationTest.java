package se.tink.integration.webdriver.service.basicutils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.google.common.collect.ImmutableMap;
import java.nio.file.Paths;
import java.util.Map;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import se.tink.backend.aggregation.nxgen.storage.AgentTemporaryStorage;
import se.tink.integration.webdriver.ChromeDriverInitializer;
import se.tink.integration.webdriver.WebDriverWrapper;

@RunWith(JUnitParamsRunner.class)
public class WebDriverBasicUtilsIntegrationTest {

    private static final String TEST_DATA_DIR =
            "src/integration/webdriver/src/test/java/se/tink/integration/webdriver/service/resources/basic_utils";

    private static WebDriverWrapper driver;
    private static WebDriverBasicUtils basicUtils;

    @BeforeClass
    public static void setup() {
        driver = ChromeDriverInitializer.constructChromeDriver(mock(AgentTemporaryStorage.class));
        basicUtils = new WebDriverBasicUtilsImpl(driver, mock(Sleeper.class));
    }

    @AfterClass
    public static void cleanup() {
        ChromeDriverInitializer.quitChromeDriver(driver);
    }

    @Test
    @Parameters(method = "paramsForElementAttributes")
    public void should_return_all_element_attributes(
            String elementId, Map<String, String> expectedAttributes) {
        // given
        driver.get(getUriToHtmlFile("element_attributes.html"));

        // when
        WebElement element = driver.findElement(By.id(elementId));
        Map<String, String> attributes = basicUtils.getElementAttributes(element);

        // then
        assertThat(attributes).containsExactlyInAnyOrderEntriesOf(expectedAttributes);
    }

    @SuppressWarnings("unused")
    private Object[] paramsForElementAttributes() {
        return new Object[] {
            new Object[] {
                "elementWithTagAttributes",
                ImmutableMap.of(
                        "id", "elementWithTagAttributes",
                        "attribute1", "value1",
                        "attribute2", "value2")
            },
            new Object[] {
                "elementWithTagAndDynamicAttributes",
                ImmutableMap.of(
                        "id", "elementWithTagAndDynamicAttributes",
                        "attribute2", "value22",
                        "attribute3", "value3")
            }
        };
    }

    @Test
    @Parameters(method = "paramsForElementVisibility")
    public void should_recognize_element_visibility(String elementId, boolean isVisibleExpected) {
        // given
        driver.get(getUriToHtmlFile("element_visibility.html"));

        // when
        WebElement element = driver.findElement(By.id(elementId));
        boolean isVisible = basicUtils.isElementVisible(element);

        // then
        assertThat(isVisible).isEqualTo(isVisibleExpected);
    }

    @SuppressWarnings("unused")
    private static Object[] paramsForElementVisibility() {
        return new Object[] {
            new Object[] {"elementWithParentDisplayNone", false},
            new Object[] {"elementWithParentHidden", false},
            new Object[] {"elementInheritVisibilityWithParentHidden", false},
            new Object[] {"elementExplicitlyVisibleWithParentHidden", true},
            new Object[] {"elementInheritVisibilityWithParentVisible", true},
            new Object[] {"elementWithParentVisible", true}
        };
    }

    private String getUriToHtmlFile(String fileName) {
        return Paths.get(TEST_DATA_DIR, fileName).toUri().toString();
    }
}
