package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.utils;

import com.google.inject.Inject;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchFrameException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdConstants;

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
            log.warn("{} Couldn't switch to iFrame", BankIdConstants.BANK_ID_LOG_PREFIX);
            return false;
        }
    }

    private Optional<WebElement> tryFindElement(By by) {
        return driver.findElements(by).stream().findFirst();
    }
}
