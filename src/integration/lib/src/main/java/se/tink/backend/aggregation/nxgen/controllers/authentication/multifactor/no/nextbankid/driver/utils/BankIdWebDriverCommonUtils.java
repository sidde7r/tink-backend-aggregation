package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.utils;

import com.google.inject.Inject;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchFrameException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class BankIdWebDriverCommonUtils {

    private final WebDriver driver;

    public void switchToParentWindow() {
        driver.switchTo().defaultContent();
    }

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
            log.warn("[BankID] Couldn't switch to iFrame");
            return false;
        }
    }

    private Optional<WebElement> tryFindElement(By by) {
        return driver.findElements(by).stream().findFirst();
    }
}
