package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants.HtmlElements.IFRAME;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.util.NemIdTestHelper.asList;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.util.NemIdTestHelper.verifyNTimes;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.util.NemIdTestHelper.webElementMock;

import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.InOrder;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriver.TargetLocator;
import org.openqa.selenium.WebElement;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.utils.ElementsSearchQuery;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.utils.ElementsSearchResult;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.utils.NemIdWebDriverWrapper;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.utils.Sleeper;

public class NemIdWebDriverWrapperTest {

    private WebDriver driver;
    private TargetLocator targetLocator;
    private Sleeper sleeper;
    private WebElement iframeElement;

    private InOrder mocksToVerifyInOrder;

    private NemIdWebDriverWrapper driverWrapper;

    @Before
    public void setup() {
        driver = mock(WebDriver.class, Answers.RETURNS_DEEP_STUBS);

        targetLocator = mock(TargetLocator.class);
        when(targetLocator.frame(any(WebElement.class))).thenReturn(driver);
        when(targetLocator.defaultContent()).thenReturn(driver);
        when(driver.switchTo()).thenReturn(targetLocator);

        sleeper = mock(Sleeper.class);
        iframeElement = mock(WebElement.class);

        mocksToVerifyInOrder = inOrder(driver, targetLocator, sleeper);

        driverWrapper = new NemIdWebDriverWrapper(driver, sleeper);
    }

    @Test
    public void should_return_false_and_dont_try_switching_to_non_existing_iframe() {
        // given
        when(driver.findElements(any())).thenReturn(Collections.emptyList());

        // when
        boolean switchResult = driverWrapper.trySwitchToNemIdIframe();

        // then
        assertThat(switchResult).isFalse();

        mocksToVerifyInOrder.verify(driver).findElements(IFRAME);
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    @Test
    public void should_return_true_and_switch_to_first_iframe_that_exists() {
        // given
        WebElement iframe1 = webElementMock();
        WebElement iframe2 = webElementMock();

        when(driver.findElements(any())).thenReturn(asList(iframe1, iframe2));

        // when
        boolean switchResult = driverWrapper.trySwitchToNemIdIframe();

        // then
        assertThat(switchResult).isTrue();

        mocksToVerifyInOrder.verify(driver).findElements(IFRAME);
        mocksToVerifyInOrder.verify(driver).switchTo();
        mocksToVerifyInOrder.verify(targetLocator).frame(iframe1);
        mocksToVerifyInOrder.verifyNoMoreInteractions();

        verifyZeroInteractions(iframe2);
    }

    @Test
    public void should_set_value_to_first_displayed_element() {
        // given
        String value = "test value";
        By by = By.id("test id");
        WebElement element1 = webElementMock(false);
        WebElement element2 = webElementMock(true);
        WebElement element3 = webElementMock(true);

        when(driver.findElements(by)).thenReturn(asList(element1, element2, element3));

        // when
        driverWrapper.setValueToElement(value, by);

        // then
        mocksToVerifyInOrder.verify(driver).findElements(by);
        mocksToVerifyInOrder.verifyNoMoreInteractions();

        verify(element1).isDisplayed();
        verifyNoMoreInteractions(element1);

        verify(element2).isDisplayed();
        verify(element2).sendKeys(value);
        verifyNoMoreInteractions(element2);

        verifyZeroInteractions(element3);
    }

    @Test
    public void should_throw_illegal_state_exception_when_setting_value_to_not_existing_element() {
        // given
        String value = "test value";
        By by = By.id("test id");

        when(driver.findElements(by)).thenReturn(Collections.emptyList());

        // when
        Throwable throwable = catchThrowable(() -> driverWrapper.setValueToElement(value, by));

        // then
        assertThat(throwable).isInstanceOf(IllegalStateException.class);

        mocksToVerifyInOrder.verify(driver).findElements(by);
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    @Test
    public void should_throw_illegal_state_exception_when_setting_value_to_not_displayed_element() {
        // given
        String value = "test value";
        By by = By.id("test id");

        WebElement element1 = webElementMock(false);
        WebElement element2 = webElementMock(false);

        when(driver.findElements(by)).thenReturn(asList(element1, element2));

        // when
        Throwable throwable = catchThrowable(() -> driverWrapper.setValueToElement(value, by));

        // then
        assertThat(throwable).isInstanceOf(IllegalStateException.class);

        mocksToVerifyInOrder.verify(driver).findElements(by);
        mocksToVerifyInOrder.verifyNoMoreInteractions();

        verify(element1).isDisplayed();
        verifyNoMoreInteractions(element1);

        verify(element2).isDisplayed();
        verifyNoMoreInteractions(element2);
    }

    @Test
    public void should_click_first_displayed_button() {
        // given
        By by = By.id("button id");
        WebElement element1 = webElementMock(false);
        WebElement element2 = webElementMock(true);
        WebElement element3 = webElementMock(true);

        when(driver.findElements(by)).thenReturn(asList(element1, element2));

        // when
        driverWrapper.clickButton(by);

        // then
        mocksToVerifyInOrder.verify(driver).findElements(by);
        mocksToVerifyInOrder.verify(sleeper).sleepFor(1000);
        mocksToVerifyInOrder.verifyNoMoreInteractions();

        verify(element1).isDisplayed();
        verifyNoMoreInteractions(element1);

        verify(element2).isDisplayed();
        verify(element2).click();
        verifyNoMoreInteractions(element2);

        verifyZeroInteractions(element3);
    }

    @Test
    public void should_throw_illegal_state_exception_when_clicking_not_existing_button() {
        // given
        By by = By.id("button id");

        when(driver.findElements(by)).thenReturn(Collections.emptyList());

        // when
        Throwable throwable = catchThrowable(() -> driverWrapper.clickButton(by));

        // then
        assertThat(throwable).isInstanceOf(IllegalStateException.class);

        mocksToVerifyInOrder.verify(driver).findElements(by);
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    @Test
    public void should_throw_illegal_state_exception_when_trying_to_click_not_displayed_button() {
        // given
        By by = By.id("button id");
        WebElement button1 = webElementMock(false);
        WebElement button2 = webElementMock(false);

        when(driver.findElements(by)).thenReturn(asList(button1, button2));

        // when
        Throwable throwable = catchThrowable(() -> driverWrapper.clickButton(by));

        // then
        assertThat(throwable).isInstanceOf(IllegalStateException.class);

        mocksToVerifyInOrder.verify(driver).findElements(by);
        mocksToVerifyInOrder.verifyNoMoreInteractions();

        verify(button1).isDisplayed();
        verifyNoMoreInteractions(button1);

        verify(button2).isDisplayed();
        verifyZeroInteractions(button2);
    }

    @Test
    public void should_wait_n_seconds_for_element_when_it_doesnt_exist() {
        // given
        By by = By.id("some id");
        when(driver.findElements(by)).thenReturn(Collections.emptyList());

        // when
        Optional<WebElement> maybeWebElement = driverWrapper.waitForElement(by, 30);

        // then
        assertThat(maybeWebElement).isEmpty();

        verifyNTimes(
                () -> {
                    mocksToVerifyInOrder.verify(driver).findElements(by);
                    mocksToVerifyInOrder.verify(sleeper).sleepFor(1_000);
                },
                30);
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    @Test
    public void should_wait_and_return_first_found_element() {
        // given
        By by = By.id("some id");
        WebElement element1 = webElementMock();
        WebElement element2 = webElementMock();

        when(driver.findElements(by))
                .thenReturn(Collections.emptyList())
                .thenReturn(Collections.emptyList())
                .thenReturn(Stream.of(element1, element2).collect(Collectors.toList()));

        // when
        Optional<WebElement> maybeWebElement = driverWrapper.waitForElement(by, 30);

        // then
        assertThat(maybeWebElement).hasValue(element1);

        verifyNTimes(
                () -> {
                    mocksToVerifyInOrder.verify(driver).findElements(by);
                    mocksToVerifyInOrder.verify(sleeper).sleepFor(1_000);
                },
                2);
        mocksToVerifyInOrder.verify(driver).findElements(by);
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    @Test
    public void should_search_for_elements_in_1_second_intervals_and_return_correct_element() {
        // given
        By parentWindowBy1 = By.id("parentWindowBy1");
        By parentWindowBy2 = By.id("parentWindowBy2");
        By iframeBy1 = By.id("iframeBy1 - WILL BE FOUND");
        By iframeBy2 = By.id("iframeBy2");

        mockThereIsNemIdIframe();

        WebElement elementToBeFound = webElementMock();
        WebElement secondElementThatWontBeFound = webElementMock();

        when(driver.findElements(parentWindowBy1)).thenReturn(Collections.emptyList());
        when(driver.findElements(parentWindowBy2)).thenReturn(Collections.emptyList());
        when(driver.findElements(iframeBy1))
                .thenReturn(Collections.emptyList())
                .thenReturn(Collections.emptyList())
                .thenReturn(asList(elementToBeFound, secondElementThatWontBeFound));
        when(driver.findElements(iframeBy2)).thenReturn(Collections.emptyList());

        // when
        ElementsSearchResult searchResult =
                driverWrapper.searchForFirstElement(
                        ElementsSearchQuery.builder()
                                .searchInParentWindow(parentWindowBy1, parentWindowBy2)
                                .searchInAnIframe(iframeBy1, iframeBy2)
                                .build());

        // then
        assertThat(searchResult).isEqualTo(ElementsSearchResult.of(iframeBy1, elementToBeFound));

        verifyNTimes(
                () -> {
                    verifySwitchingToParentWindow();
                    mocksToVerifyInOrder.verify(driver).findElements(parentWindowBy1);
                    mocksToVerifyInOrder.verify(driver).findElements(parentWindowBy2);

                    verifySwitchingToIframe();
                    mocksToVerifyInOrder.verify(driver).findElements(iframeBy1);
                    mocksToVerifyInOrder.verify(driver).findElements(iframeBy2);

                    mocksToVerifyInOrder.verify(sleeper).sleepFor(1_000);
                },
                2);
        verifySwitchingToParentWindow();
        mocksToVerifyInOrder.verify(driver).findElements(parentWindowBy1);
        mocksToVerifyInOrder.verify(driver).findElements(parentWindowBy2);

        verifySwitchingToIframe();
        mocksToVerifyInOrder.verify(driver).findElements(iframeBy1);

        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    @Test
    public void should_search_not_search_for_elements_in_iframe_if_it_doesnt_exist() {
        // given
        By parentWindowBy1 = By.id("parentWindowBy1");
        By parentWindowBy2 = By.id("parentWindowBy2  - WILL BE FOUND");
        By iframeBy1 = By.id("iframeBy1");
        By iframeBy2 = By.id("iframeBy2");

        mockThereIsNoNemIdIframe();

        WebElement elementToBeFound = webElementMock();
        WebElement secondElementThatWontBeFound = webElementMock();

        when(driver.findElements(parentWindowBy1)).thenReturn(Collections.emptyList());
        when(driver.findElements(parentWindowBy2))
                .thenReturn(Collections.emptyList())
                .thenReturn(Collections.emptyList())
                .thenReturn(asList(elementToBeFound, secondElementThatWontBeFound));

        // when
        ElementsSearchResult searchResult =
                driverWrapper.searchForFirstElement(
                        ElementsSearchQuery.builder()
                                .searchInParentWindow(parentWindowBy1, parentWindowBy2)
                                .searchInAnIframe(iframeBy1, iframeBy2)
                                .build());

        // then
        assertThat(searchResult)
                .isEqualTo(ElementsSearchResult.of(parentWindowBy2, elementToBeFound));

        verifyNTimes(
                () -> {
                    verifySwitchingToParentWindow();
                    mocksToVerifyInOrder.verify(driver).findElements(parentWindowBy1);
                    mocksToVerifyInOrder.verify(driver).findElements(parentWindowBy2);

                    verifyTryingSwitchingToIframe();

                    mocksToVerifyInOrder.verify(sleeper).sleepFor(1_000);
                },
                2);
        verifySwitchingToParentWindow();
        mocksToVerifyInOrder.verify(driver).findElements(parentWindowBy1);
        mocksToVerifyInOrder.verify(driver).findElements(parentWindowBy2);

        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    @Test
    public void should_search_for_elements_and_return_empty_result_after_timeout() {
        // given
        By by = By.id("by");
        when(driver.findElements(by)).thenReturn(Collections.emptyList());

        // when
        ElementsSearchResult searchResult =
                driverWrapper.searchForFirstElement(
                        ElementsSearchQuery.builder()
                                .searchInParentWindow(by)
                                .searchForSeconds(10)
                                .build());

        // then
        assertThat(searchResult).isEqualTo(ElementsSearchResult.empty());

        verifyNTimes(
                () -> {
                    verifySwitchingToParentWindow();
                    mocksToVerifyInOrder.verify(driver).findElements(by);

                    verifyTryingSwitchingToIframe();

                    mocksToVerifyInOrder.verify(sleeper).sleepFor(1_000);
                },
                10);
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    private void mockThereIsNemIdIframe() {
        when(driver.findElements(IFRAME)).thenReturn(asList(iframeElement));
    }

    private void mockThereIsNoNemIdIframe() {
        when(driver.findElements(IFRAME)).thenReturn(Collections.emptyList());
    }

    private void verifySwitchingToParentWindow() {
        mocksToVerifyInOrder.verify(driver).switchTo();
        mocksToVerifyInOrder.verify(targetLocator).defaultContent();
    }

    private void verifySwitchingToIframe() {
        mocksToVerifyInOrder.verify(driver).findElements(IFRAME);
        mocksToVerifyInOrder.verify(driver).switchTo();
        mocksToVerifyInOrder.verify(targetLocator).frame(iframeElement);
    }

    private void verifyTryingSwitchingToIframe() {
        mocksToVerifyInOrder.verify(driver).findElements(IFRAME);
    }
}
