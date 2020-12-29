package se.tink.libraries.selenium;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriver.TargetLocator;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import se.tink.libraries.selenium.exceptions.HtmlElementNotFoundException;
import se.tink.libraries.selenium.exceptions.ScreenScrapingException;

public class WebDriverHelperTest {
    private WebDriver driver;
    private WebDriverHelper webDriverHelper;

    @Before
    public void init() {
        TargetLocator targetLocator = mock(TargetLocator.class);
        driver = mock(PhantomJSDriver.class);
        webDriverHelper = new WebDriverHelper(100);
        given(driver.switchTo()).willReturn(targetLocator);
    }

    @Test
    public void getElementShouldThrowExceptionWhenNoElementFound() {
        // given
        By xpath = By.xpath("dummy");

        // when
        Throwable throwable = catchThrowable(() -> webDriverHelper.getElement(driver, xpath));

        // then
        assertThat(throwable)
                .isInstanceOf(HtmlElementNotFoundException.class)
                .hasMessage("Can't find element By.xpath: dummy");
    }

    @Test
    public void clickButtonShouldThrowExceptionWhenNotInteractable() {
        // given

        WebElement element = mock(WebElement.class);
        given(element.isEnabled()).willReturn(false);
        given(element.toString()).willReturn("dummy_button");
        // when

        Throwable throwable = catchThrowable(() -> webDriverHelper.clickButton(element));

        // then
        assertThat(throwable)
                .isInstanceOf(ScreenScrapingException.class)
                .hasMessage("Button dummy_button is not interactable");
    }

    @Test
    public void clickButtonShouldNotThrowExceptionWhenElementGotEnabled() {
        // given

        WebElement element = mock(WebElement.class);
        given(element.isEnabled()).willReturn(false).willReturn(true);
        // when

        Throwable throwable = catchThrowable(() -> webDriverHelper.clickButton(element));

        // then
        assertThat(throwable).isNull();
    }

    @Test
    public void switchToIframeShouldThrowIfIframeNotFound() {
        // given

        // when
        Throwable throwable = catchThrowable(() -> webDriverHelper.switchToIframe(driver));

        // then
        assertThat(throwable)
                .isInstanceOf(HtmlElementNotFoundException.class)
                .hasMessage("Can't find element By.tagName: iframe");
    }

    @Test
    public void switchToIframeShouldSwitchToIFrameIfAvailable() {
        // given
        WebElement iframe = mock(WebElement.class);
        given(iframe.isDisplayed()).willReturn(true);
        given(driver.findElements(By.tagName("iframe")))
                .willReturn(Collections.singletonList(iframe));

        // when
        Throwable throwable = catchThrowable(() -> webDriverHelper.switchToIframe(driver));

        // then
        assertThat(throwable).isNull();
    }
}
