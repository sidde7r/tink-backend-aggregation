package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver;

import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.BankIdConstants.HtmlElements.BY_IFRAME;

import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.NoSuchFrameException;
import org.openqa.selenium.WebDriver;
import se.tink.integration.webdriver.utils.Sleeper;
import se.tink.integration.webdriver.utils.WebDriverWrapperImpl;

@Slf4j
public class BankIdWebDriver extends WebDriverWrapperImpl {

    @Inject
    public BankIdWebDriver(WebDriver webDriver, Sleeper sleeper) {
        super(webDriver, sleeper);
    }

    public boolean trySwitchToIframe() {
        switchToParentWindow();
        try {
            return trySwitchToFrame(BY_IFRAME);

        } catch (NoSuchFrameException e) {
            log.warn("[BankID] Couldn't switch to iFrame");
            return false;
        }
    }

    public String getFullPageSourceLog() {
        switchToParentWindow();
        String mainPageSource = getPageSource();

        boolean switchedToIframe = trySwitchToIframe();
        String iframeSource = switchedToIframe ? getPageSource() : null;

        return String.format(
                "Main page source:%n" + "%s" + "%nBankID iframe source:%n" + "%s",
                mainPageSource, iframeSource);
    }
}
