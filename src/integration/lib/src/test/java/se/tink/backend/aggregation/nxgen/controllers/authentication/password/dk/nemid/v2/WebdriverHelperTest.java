package se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.v2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.phantomjs.PhantomJSDriver;

public class WebdriverHelperTest {

    private static final By BY_ID = By.id("sample-id");

    private WebDriver driver;
    private Sleeper sleeper;

    private WebdriverHelper helper;

    @Before
    public void setUp() {

        driver = mock(WebDriver.class, Answers.RETURNS_DEEP_STUBS);
        sleeper = mock(Sleeper.class);

        helper = new WebdriverHelper(sleeper);
    }

    @Test
    public void constructWebDriver() {
        // given

        // when
        WebDriver result = helper.constructWebDriver(5_000);

        // then
        assertThat(result).isInstanceOf(PhantomJSDriver.class);
    }

    @Test
    public void waitForElementShouldReturnEmptyOptionalWhenDriverHasNoDisplayedElements() {
        // given
        List<WebElement> webElements = Arrays.asList(createNotDisplayed(), createNotDisplayed());
        // and
        given(driver.findElements(BY_ID)).willReturn(webElements);

        // when
        Optional<WebElement> result = helper.waitForElement(driver, BY_ID);

        // then
        assertThat(result).isEqualTo(Optional.empty());
        verify(driver).findElements(BY_ID);
    }

    @Test
    public void waitForElementShouldReturnFirstDisplayedElement() {
        // given
        WebElement firstDisplayedElement = createDisplayed();
        WebElement secondDisplayedElement = createDisplayed();
        List<WebElement> webElements =
                Arrays.asList(
                        createNotDisplayed(),
                        firstDisplayedElement,
                        createNotDisplayed(),
                        secondDisplayedElement);
        // and
        given(driver.findElements(BY_ID)).willReturn(webElements);

        // when
        Optional<WebElement> result = helper.waitForElement(driver, BY_ID);

        // then
        assertThat(result.isPresent()).isTrue();
        assertThat(result.get()).isEqualTo(firstDisplayedElement);
    }

    @Test
    public void waitForElementShouldReturnElementWhenSecondFindOnDriverIsCalled() {
        // given
        WebElement firstDisplayedElement = createDisplayed();
        WebElement secondDisplayedElement = createDisplayed();
        List<WebElement> webElements =
                Arrays.asList(
                        createNotDisplayed(),
                        firstDisplayedElement,
                        createNotDisplayed(),
                        secondDisplayedElement);
        // and
        given(driver.findElements(BY_ID))
                .willReturn(Collections.emptyList())
                .willReturn(webElements);

        // when
        Optional<WebElement> result = helper.waitForElement(driver, BY_ID);

        // then
        assertThat(result.isPresent()).isTrue();
        assertThat(result.get()).isEqualTo(firstDisplayedElement);
        // and
        verify(driver, times(2)).findElements(BY_ID);
    }

    @Test
    public void waitForElementsShouldWaitBeforeSecondFindOnDriverIsCalled() {
        // given
        given(driver.findElements(BY_ID))
                .willReturn(Collections.emptyList())
                .willReturn(Collections.emptyList());
        // and
        InOrder inOrder = Mockito.inOrder(driver, sleeper);

        // when
        helper.waitForElement(driver, BY_ID);

        // then
        inOrder.verify(driver).findElements(BY_ID);
        inOrder.verify(sleeper).sleepFor(5_000);
        inOrder.verify(driver).findElements(BY_ID);
    }

    @Test
    public void waitForElementShouldReturnEmptyOptionalWhenDriverHasNoElements() {
        // given
        given(driver.findElements(BY_ID))
                .willReturn(Collections.emptyList())
                .willReturn(Collections.emptyList());

        // when
        helper.waitForElement(driver, BY_ID);

        // then
        verify(driver, times(2)).findElements(BY_ID);
    }

    @Test
    public void setValueToElementShouldThrowExceptionWhenDesiredElementIsNotPresent() {
        // given
        String value = "sample value";
        // and
        given(driver.findElements(BY_ID)).willReturn(Collections.emptyList());

        // when
        Throwable throwable = catchThrowable(() -> helper.setValueToElement(driver, value, BY_ID));

        // then
        assertThat(throwable)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Could not find element for " + BY_ID.toString());
    }

    @Test
    public void setValueToElementShouldSetValueForFoundElement() {
        // given
        String value = "sample value";
        // and
        WebElement webElement = createDisplayed();
        given(driver.findElements(BY_ID)).willReturn(Collections.singletonList(webElement));

        // when
        helper.setValueToElement(driver, value, BY_ID);

        // then
        verify(webElement).sendKeys(value);
    }

    @Test
    public void clickButtonShouldThrowExceptionWhenDesiredElementIsNotPresent() {
        // given
        given(driver.findElements(BY_ID)).willReturn(Collections.emptyList());

        // when
        Throwable throwable = catchThrowable(() -> helper.clickButton(driver, BY_ID));

        // then
        assertThat(throwable)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Could not find button element " + BY_ID.toString());
    }

    @Test
    public void clickButtonShouldClickFoundElement() {
        // given
        WebElement webElement = createDisplayed();
        given(driver.findElements(BY_ID)).willReturn(Collections.singletonList(webElement));

        // when
        helper.clickButton(driver, BY_ID);

        // then
        verify(webElement).click();
    }

    private WebElement createNotDisplayed() {
        WebElement notDisplayed = mock(WebElement.class);
        given(notDisplayed.isDisplayed()).willReturn(false);
        return notDisplayed;
    }

    private WebElement createDisplayed() {
        WebElement displayed = mock(WebElement.class);
        given(displayed.isDisplayed()).willReturn(true);
        return displayed;
    }
}
