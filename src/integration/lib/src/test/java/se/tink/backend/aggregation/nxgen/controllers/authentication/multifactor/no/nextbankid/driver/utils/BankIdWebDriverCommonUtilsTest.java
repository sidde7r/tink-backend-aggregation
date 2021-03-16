package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.utils;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchFrameException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class BankIdWebDriverCommonUtilsTest {

    /*
    Mocks
     */
    private WebDriver driver;
    private WebDriver.TargetLocator targetLocator;

    private InOrder mocksToVerifyInOrder;

    /*
    Real
     */
    private BankIdWebDriverCommonUtils driverCommonUtils;

    @Before
    public void setup() {
        driver = mock(WebDriver.class);
        targetLocator = mock(WebDriver.TargetLocator.class);
        when(driver.switchTo()).thenReturn(targetLocator);

        mocksToVerifyInOrder = inOrder(driver, targetLocator);

        driverCommonUtils = new BankIdWebDriverCommonUtils(driver);
    }

    @Test
    public void should_switch_to_parent_window() {
        // when
        driverCommonUtils.switchToParentWindow();

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
        boolean hasSwitched = driverCommonUtils.trySwitchToIframe(iframeSelector);

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
        boolean hasSwitched = driverCommonUtils.trySwitchToIframe(iframeSelector);

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
        boolean hasSwitched = driverCommonUtils.trySwitchToIframe(iframeSelector);

        // then
        assertThat(hasSwitched).isFalse();

        mocksToVerifyInOrder.verify(driver).findElements(iframeSelector);
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }
}
