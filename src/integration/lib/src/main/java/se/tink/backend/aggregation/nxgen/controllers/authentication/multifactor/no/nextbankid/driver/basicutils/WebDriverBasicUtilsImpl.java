package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.basicutils;

import com.google.inject.Inject;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.NoSuchFrameException;
import org.openqa.selenium.WebElement;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.WebDriverConstants;
import se.tink.integration.webdriver.WebDriverWrapper;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class WebDriverBasicUtilsImpl implements WebDriverBasicUtils {

    private final WebDriverWrapper driver;
    private final Sleeper sleeper;

    @Override
    public Set<Cookie> getCookies() {
        return driver.manage().getCookies();
    }

    @Override
    public void switchToParentWindow() {
        driver.switchTo().defaultContent();
    }

    @Override
    public boolean trySwitchToIframe(By iframeSelector) {
        try {
            return tryFindElement(iframeSelector)
                    .map(
                            element -> {
                                driver.switchTo().frame(element);
                                return true;
                            })
                    .orElse(false);
        } catch (NoSuchFrameException e) {
            log.warn("{} Couldn't switch to iFrame", WebDriverConstants.LOG_TAG);
            return false;
        }
    }

    @Override
    public Optional<WebElement> tryFindElement(By by) {
        return driver.findElements(by).stream().findFirst();
    }

    @Override
    public void sleepFor(int millis) {
        sleeper.sleepFor(millis);
    }
}
