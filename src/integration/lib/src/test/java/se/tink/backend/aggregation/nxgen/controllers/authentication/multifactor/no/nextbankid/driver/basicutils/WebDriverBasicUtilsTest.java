package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.basicutils;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.NoSuchFrameException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import se.tink.integration.webdriver.WebDriverWrapper;

public class WebDriverBasicUtilsTest {

    /*
    Mocks
     */
    private WebDriverWrapper driver;
    private WebDriver.TargetLocator targetLocator;
    private WebDriver.Options driverOptions;

    private InOrder mocksToVerifyInOrder;

    /*
    Real
     */
    private WebDriverBasicUtils basicUtils;

    @Before
    public void setup() {
        driver = mock(WebDriverWrapper.class);

        targetLocator = mock(WebDriver.TargetLocator.class);
        when(driver.switchTo()).thenReturn(targetLocator);

        driverOptions = mock(WebDriver.Options.class);
        when(driver.manage()).thenReturn(driverOptions);

        mocksToVerifyInOrder = inOrder(driver, targetLocator, driverOptions);

        basicUtils = new WebDriverBasicUtilsImpl(driver, mock(Sleeper.class));
    }

    @Test
    public void should_switch_to_parent_window() {
        // when
        basicUtils.switchToParentWindow();

        // then
        mocksToVerifyInOrder.verify(driver).switchTo();
        mocksToVerifyInOrder.verify(targetLocator).defaultContent();
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    @Test
    public void should_return_true_and_switch_to_first_iframe_matching_selector() {
        // given
        WebElement iframeElement1 = mock(WebElement.class);
        WebElement iframeElement2 = mock(WebElement.class);

        when(driver.findElements(any())).thenReturn(asList(iframeElement1, iframeElement2));

        // when
        By iframeSelector = mock(By.class);
        boolean hasSwitched = basicUtils.trySwitchToIframe(iframeSelector);

        // then
        assertThat(hasSwitched).isTrue();

        mocksToVerifyInOrder.verify(driver).findElements(iframeSelector);
        mocksToVerifyInOrder.verify(driver).switchTo();
        mocksToVerifyInOrder.verify(targetLocator).frame(iframeElement1);
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    @Test
    public void should_return_false_when_we_can_find_iframe_but_its_not_possible_to_switch_to_it() {
        // given
        // given
        WebElement iframeElement1 = mock(WebElement.class);
        WebElement iframeElement2 = mock(WebElement.class);

        when(driver.findElements(any())).thenReturn(asList(iframeElement1, iframeElement2));
        doThrow(new NoSuchFrameException("whatever reason"))
                .when(targetLocator)
                .frame(any(WebElement.class));

        // when
        By iframeSelector = mock(By.class);
        boolean hasSwitched = basicUtils.trySwitchToIframe(iframeSelector);

        // then
        assertThat(hasSwitched).isFalse();

        mocksToVerifyInOrder.verify(driver).findElements(iframeSelector);
        mocksToVerifyInOrder.verify(driver).switchTo();
        mocksToVerifyInOrder.verify(targetLocator).frame(iframeElement1);
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    @Test
    public void should_return_false_when_we_cant_find_iframe() {
        // given
        when(driver.findElements(any())).thenReturn(emptyList());

        // when
        By iframeSelector = mock(By.class);
        boolean hasSwitched = basicUtils.trySwitchToIframe(iframeSelector);

        // then
        assertThat(hasSwitched).isFalse();

        mocksToVerifyInOrder.verify(driver).findElements(iframeSelector);
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    @Test
    public void should_get_cookies() {
        // given
        Cookie cookie1 = mock(Cookie.class);
        Cookie cookie2 = mock(Cookie.class);
        Set<Cookie> expectedCookies = ImmutableSet.of(cookie1, cookie2);

        when(driverOptions.getCookies()).thenReturn(expectedCookies);

        // when
        Set<Cookie> cookies = basicUtils.getCookies();

        // then
        assertThat(cookies).isEqualTo(expectedCookies);

        mocksToVerifyInOrder.verify(driver).manage();
        mocksToVerifyInOrder.verify(driverOptions).getCookies();
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }
}
