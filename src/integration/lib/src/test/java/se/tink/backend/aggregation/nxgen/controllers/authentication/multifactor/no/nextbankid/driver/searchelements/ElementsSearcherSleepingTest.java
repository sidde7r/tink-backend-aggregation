package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.searchelements;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.searchelements.ExamplePageData.EXAMPLE_HTML_PAGE_URL;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.searchelements.ExamplePageData.PARENT_OTHER_ELEMENT;

import java.util.function.Consumer;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.basicutils.Sleeper;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.basicutils.WebDriverBasicUtils;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.basicutils.WebDriverBasicUtilsImpl;
import se.tink.integration.webdriver.ChromeDriverInitializer;
import se.tink.integration.webdriver.WebDriverWrapper;

/**
 * The goal of this test class is to check that {@link ElementsSearcher} will sleep for the correct
 * number of seconds while trying to find required elements.
 */
@RunWith(JUnitParamsRunner.class)
public class ElementsSearcherSleepingTest {

    private static WebDriverWrapper driver;
    private static JavascriptExecutor jsExecutor;

    private SleeperWithSleepsCounter sleeper;
    private ElementsSearcher elementsSearcher;

    @BeforeClass
    public static void setupDriver() {
        WebDriverWrapper webDriver = ChromeDriverInitializer.constructChromeDriver();
        driver = spy(webDriver);
        jsExecutor = webDriver;
    }

    @AfterClass
    public static void quitDriver() {
        ChromeDriverInitializer.quitChromeDriver(driver);
    }

    @Before
    public void setupTest() {
        sleeper = spy(new SleeperWithSleepsCounter());
        WebDriverBasicUtils driverBasicUtils = new WebDriverBasicUtilsImpl(driver, sleeper);
        elementsSearcher = new ElementsSearcherImpl(driver, driverBasicUtils);

        driver.get(EXAMPLE_HTML_PAGE_URL);
    }

    @Test
    @Parameters(value = {"10", "15", "25"})
    public void should_wait_for_n_seconds_and_return_empty_result(int waitForSeconds) {
        // given
        ElementLocator notExistingLocator =
                ElementLocator.builder()
                        .element(new By.ByCssSelector(".notExistingClass1234"))
                        .build();

        // when
        ElementsSearchResult searchResult =
                elementsSearcher.searchForFirstMatchingLocator(
                        ElementsSearchQuery.builder()
                                .searchFor(notExistingLocator)
                                .searchForSeconds(waitForSeconds)
                                .build());
        // then
        assertThat(searchResult.isEmpty()).isTrue();

        verify(sleeper, times(waitForSeconds)).sleepFor(1_000);
    }

    @Test
    public void should_not_sleep_when_there_is_search_only_once_flag_set() {
        // given
        ElementLocator notExistingLocator =
                ElementLocator.builder()
                        .element(new By.ByCssSelector(".notExistingClass1234567"))
                        .build();

        // when
        ElementsSearchResult searchResult =
                elementsSearcher.searchForFirstMatchingLocator(
                        ElementsSearchQuery.builder()
                                .searchFor(notExistingLocator)
                                .searchForSeconds(10)
                                .searchOnlyOnce()
                                .build());
        // then
        assertThat(searchResult.isEmpty()).isTrue();

        verifyZeroInteractions(sleeper);
    }

    @Test
    public void should_wait_until_element_is_found_and_return_correct_result() {
        // given
        WebElement parentOtherElement = driver.findElement(By.id(PARENT_OTHER_ELEMENT));
        addClassToElement(parentOtherElement, "hidden");

        sleeper.setSleepsCountListener(
                counter -> {
                    if (counter == 5) {
                        removeClassFromElement(parentOtherElement, "hidden");
                    }
                });

        // when
        ElementLocator elementLocator =
                ElementLocator.builder()
                        .element(new By.ByCssSelector("#" + PARENT_OTHER_ELEMENT))
                        .mustBeDisplayed()
                        .build();
        ElementsSearchResult searchResult =
                elementsSearcher.searchForFirstMatchingLocator(
                        ElementsSearchQuery.builder()
                                .searchFor(elementLocator)
                                .searchForSeconds(20)
                                .build());
        // then
        assertThat(searchResult.isEmpty()).isFalse();
        assertThat(searchResult.getLocatorFound()).isEqualTo(elementLocator);

        verify(sleeper, times(5)).sleepFor(1_000);
    }

    @SuppressWarnings("SameParameterValue")
    private static void addClassToElement(WebElement element, String className) {
        String newClassValue = element.getAttribute("class") + " " + className;
        jsExecutor.executeScript(
                "arguments[0].setAttribute(arguments[1], arguments[2]);",
                element,
                "class",
                newClassValue);
    }

    @SuppressWarnings("SameParameterValue")
    private static void removeClassFromElement(WebElement element, String className) {
        String newClassValue = element.getAttribute("class").replace(className, "");
        jsExecutor.executeScript(
                "arguments[0].setAttribute(arguments[1], arguments[2]);",
                element,
                "class",
                newClassValue);
    }

    @RequiredArgsConstructor
    private static class SleeperWithSleepsCounter extends Sleeper {

        private Integer sleepsCount = 0;
        @Setter private Consumer<Integer> sleepsCountListener;

        @Override
        public void sleepFor(final long millis) {
            sleepsCount += 1;
            if (sleepsCountListener != null) {
                sleepsCountListener.accept(sleepsCount);
            }
        }
    }
}
