package se.tink.integration.webdriver.service;

import com.google.inject.Inject;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import se.tink.integration.webdriver.WebDriverWrapper;
import se.tink.integration.webdriver.service.basicutils.WebDriverBasicUtils;
import se.tink.integration.webdriver.service.proxy.ProxyManager;
import se.tink.integration.webdriver.service.searchelements.ElementLocator;
import se.tink.integration.webdriver.service.searchelements.ElementsSearchQuery;
import se.tink.integration.webdriver.service.searchelements.ElementsSearcher;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PACKAGE, onConstructor = @__({@Inject}))
public class WebDriverServiceImpl implements WebDriverService {

    @Delegate private final WebDriverWrapper driver;
    @Delegate private final WebDriverBasicUtils driverBasicUtils;
    @Delegate private final ElementsSearcher elementsSearcher;
    @Delegate private final ProxyManager proxyManager;

    @Override
    public String getFullPageSourceLog(By iframeSelector) {
        driverBasicUtils.switchToParentWindow();
        String mainPageSource = driver.getPageSource();

        boolean switchedToIframe = driverBasicUtils.trySwitchToIframe(iframeSelector);
        String iframeSource = switchedToIframe ? driver.getPageSource() : null;

        return String.format(
                "Main page source:%n" + "%s" + "%nIframe source:%n" + "%s",
                mainPageSource, iframeSource);
    }

    @Override
    public void clickButton(ElementLocator locator) {
        try {
            /*
            Sometimes there is no effect of clicking the button. The exact reason was not investigated but an effective
            solution is to wait for a little to give page time to "stabilize" itself, e.g. recalculate some styles.
            It may be also helpful in avoiding org.openqa.selenium.ElementClickInterceptedException when something
            is briefly covering the button.
             */
            sleepFor(200);
            clickButtonInternal(locator);

        } catch (StaleElementReferenceException exception) {
            /*
            Sometimes element can become stale even though we have just found it. This may happen due to some
            style recalculations that cause element to be recreated.
            If such thing occurs, we should try to click button one more time.
             */
            log.warn("{} Stale button element reference", WebDriverConstants.LOG_TAG);
            clickButtonInternal(locator);
        }
    }

    private void clickButtonInternal(ElementLocator locator) {
        WebElement buttonElement =
                elementsSearcher
                        .searchForFirstMatchingLocator(
                                ElementsSearchQuery.builder()
                                        .searchFor(locator)
                                        .searchForSeconds(10)
                                        .build())
                        .getFirstFoundElement()
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "Could not find button element by " + locator));

        buttonElement.click();
    }

    @Override
    public void setValueToElement(String value, ElementLocator locator) {
        WebElement element =
                elementsSearcher
                        .searchForFirstMatchingLocator(
                                ElementsSearchQuery.builder()
                                        .searchFor(locator)
                                        .searchForSeconds(10)
                                        .build())
                        .getFirstFoundElement()
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "Could not find element by " + locator));

        element.sendKeys(value);
    }
}
