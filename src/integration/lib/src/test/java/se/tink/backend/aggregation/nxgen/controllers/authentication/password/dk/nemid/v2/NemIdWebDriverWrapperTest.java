package se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.v2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.v2.NemIdConstantsV2.HtmlElements.IFRAME;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.v2.util.NemIdTestHelper.asList;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.v2.util.NemIdTestHelper.verifyNTimes;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.v2.util.NemIdTestHelper.webElementMock;

import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriver.TargetLocator;
import org.openqa.selenium.WebElement;

public class NemIdWebDriverWrapperTest {

    private WebDriver driver;
    private TargetLocator targetLocator;
    private Sleeper sleeper;

    private NemIdWebDriverWrapper driverWrapper;

    @Before
    public void setup() {
        driver = mock(WebDriver.class, Answers.RETURNS_DEEP_STUBS);

        targetLocator = mock(TargetLocator.class);
        when(targetLocator.frame(any(WebElement.class))).thenReturn(driver);
        when(targetLocator.defaultContent()).thenReturn(driver);
        when(driver.switchTo()).thenReturn(targetLocator);

        sleeper = mock(Sleeper.class);

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

        verify(driver).findElements(IFRAME);
        verifyNoMoreInteractions(driver);
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

        verify(driver).findElements(IFRAME);
        verify(driver).switchTo();
        verify(targetLocator).frame(iframe1);
        verifyNoMoreInteractions(driver, targetLocator);

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
        verify(driver).findElements(by);
        verifyNoMoreInteractions(driver);

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

        verify(driver).findElements(by);
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

        verify(driver).findElements(by);

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
        verify(driver).findElements(by);
        verifyNoMoreInteractions(driver);

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

        verify(driver).findElements(by);
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

        verify(driver).findElements(by);

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

        InOrder inOrder = Mockito.inOrder(driver, sleeper);
        verifyNTimes(
                () -> {
                    inOrder.verify(driver).findElements(by);
                    inOrder.verify(sleeper).sleepFor(1_000);
                },
                30);
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

        InOrder inOrder = Mockito.inOrder(driver, sleeper);
        verifyNTimes(
                () -> {
                    inOrder.verify(driver).findElements(by);
                    inOrder.verify(sleeper).sleepFor(1_000);
                },
                2);
        inOrder.verify(driver).findElements(by);
    }
}
