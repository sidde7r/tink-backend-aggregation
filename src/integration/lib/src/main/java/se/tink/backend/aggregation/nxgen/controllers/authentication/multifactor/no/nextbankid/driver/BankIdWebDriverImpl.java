package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver;

import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdConstants.BANK_ID_LOG_PREFIX;

import com.google.inject.Inject;
import java.util.Set;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.searchelements.BankIdElementLocator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.searchelements.BankIdElementsSearchQuery;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.searchelements.BankIdElementsSearchResult;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.searchelements.BankIdElementsSearcher;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.utils.BankIdWebDriverCommonUtils;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.utils.Sleeper;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PACKAGE, onConstructor = @__({@Inject}))
public class BankIdWebDriverImpl implements BankIdWebDriver {

    private final WebDriver driver;
    private final Sleeper sleeper;
    private final BankIdWebDriverCommonUtils driverCommonUtils;
    private final BankIdElementsSearcher elementsSearcher;

    @Override
    public void getUrl(String url) {
        driver.get(url);
    }

    @Override
    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    @Override
    public void quitDriver() {
        driver.quit();
    }

    @Override
    public Set<Cookie> getCookies() {
        return driver.manage().getCookies();
    }

    @Override
    public String getFullPageSourceLog() {
        driverCommonUtils.switchToParentWindow();
        String mainPageSource = driver.getPageSource();

        boolean switchedToIframe =
                driverCommonUtils.trySwitchToIframe(BankIdConstants.HtmlSelectors.BY_IFRAME);
        String iframeSource = switchedToIframe ? driver.getPageSource() : null;

        return String.format(
                "Main page source:%n" + "%s" + "%nBankID iframe source:%n" + "%s",
                mainPageSource, iframeSource);
    }

    @Override
    public void clickButton(BankIdElementLocator locator) {
        try {
            clickButtonInternal(locator);

        } catch (StaleElementReferenceException exception) {
            /*
            Sometimes element can become stale even though we have just found it. This may happen due to some
            style recalculations that cause element to be recreated.
            If such thing occurs, we should try to click button one more time.
             */
            log.warn("{} Stale button element reference", BANK_ID_LOG_PREFIX);
            clickButtonInternal(locator);
        }
    }

    private void clickButtonInternal(BankIdElementLocator locator) {
        WebElement buttonElement =
                elementsSearcher
                        .searchForFirstMatchingLocator(
                                BankIdElementsSearchQuery.builder()
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
    public void setValueToElement(String value, BankIdElementLocator locator) {
        WebElement element =
                elementsSearcher
                        .searchForFirstMatchingLocator(
                                BankIdElementsSearchQuery.builder()
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

    @Override
    public void sleepFor(int millis) {
        sleeper.sleepFor(millis);
    }

    @Override
    public BankIdElementsSearchResult searchForFirstMatchingLocator(
            BankIdElementsSearchQuery query) {
        return elementsSearcher.searchForFirstMatchingLocator(query);
    }
}
