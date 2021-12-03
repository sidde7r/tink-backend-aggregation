package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver;

import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdConstants.BANK_ID_LOG_PREFIX;

import com.google.inject.Inject;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.basicutils.WebDriverBasicUtils;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.searchelements.ElementLocator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.searchelements.ElementsSearchQuery;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.searchelements.ElementsSearcher;
import se.tink.integration.webdriver.WebDriverWrapper;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PACKAGE, onConstructor = @__({@Inject}))
public class BankIdWebDriverImpl implements BankIdWebDriver {

    @Delegate private final WebDriverWrapper driver;
    @Delegate private final WebDriverBasicUtils driverBasicUtils;
    @Delegate private final ElementsSearcher elementsSearcher;

    @Override
    public String getFullPageSourceLog() {
        driverBasicUtils.switchToParentWindow();
        String mainPageSource = driver.getPageSource();

        boolean switchedToIframe =
                driverBasicUtils.trySwitchToIframe(BankIdConstants.HtmlSelectors.BY_IFRAME);
        String iframeSource = switchedToIframe ? driver.getPageSource() : null;

        return String.format(
                "Main page source:%n" + "%s" + "%nBankID iframe source:%n" + "%s",
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
            log.warn("{} Stale button element reference", BANK_ID_LOG_PREFIX);
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
