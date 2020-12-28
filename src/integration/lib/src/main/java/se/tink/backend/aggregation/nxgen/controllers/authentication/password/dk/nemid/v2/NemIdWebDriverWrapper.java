package se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.v2;

import static se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.v2.NemIdConstantsV2.HtmlElements.IFRAME;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import se.tink.libraries.pair.Pair;

@Slf4j
@RequiredArgsConstructor
public class NemIdWebDriverWrapper {

    private final WebDriver driver;
    private final Sleeper sleeper;

    public void get(String url) {
        driver.get(url);
    }

    public void quitDriver() {
        driver.quit();
    }

    public void switchToParentWindow() {
        driver.switchTo().defaultContent();
    }

    public boolean trySwitchToNemIdIframe() {
        return tryFindElement(IFRAME)
                .map(
                        element -> {
                            driver.switchTo().frame(element);
                            return true;
                        })
                .orElse(false);
    }

    public void executeScript(String script) {
        ((JavascriptExecutor) driver).executeScript(script);
    }

    public String getFullPageSourceLog() {
        switchToParentWindow();
        String mainPageSource = driver.getPageSource();

        boolean switchedToIframe = trySwitchToNemIdIframe();
        String nemIdIframeSource = switchedToIframe ? driver.getPageSource() : null;

        return String.format(
                "Main page source:%n" + "%s" + "%nNemId iframe source:%n" + "%s",
                mainPageSource, nemIdIframeSource);
    }

    public void sleepFor(long millis) {
        sleeper.sleepFor(millis);
    }

    public void setValueToElement(String value, By by) {
        WebElement element =
                tryFindDisplayedElement(by)
                        .orElseGet(
                                () -> {
                                    log.error(
                                            "Could not find displayed element by: {}. Page source:\n{}",
                                            by,
                                            getFullPageSourceLog());
                                    throw new IllegalStateException(
                                            "Could not find element by " + by);
                                });
        element.sendKeys(value);
    }

    public void clickButton(By by) {
        tryFindDisplayedElement(by)
                .orElseThrow(
                        () -> {
                            log.error(
                                    "Could not find displayed button by: {}. Page source:\n{}",
                                    by,
                                    getFullPageSourceLog());
                            return new IllegalStateException(
                                    "Could not find button element by " + by);
                        })
                .click();
    }

    public Optional<WebElement> waitForElement(By by, int seconds) {
        for (int i = 0; i < seconds; i++) {
            Optional<WebElement> maybeWebElement = tryFindElement(by);
            if (maybeWebElement.isPresent()) {
                return maybeWebElement;
            }
            sleepFor(1_000);
        }
        return Optional.empty();
    }

    public Optional<Pair<By, WebElement>> searchForFirstElement(By... byArray) {
        for (By by : byArray) {
            Optional<WebElement> maybeElementFound = tryFindElement(by);
            if (maybeElementFound.isPresent()) {
                return Optional.of(Pair.of(by, maybeElementFound.get()));
            }
        }
        return Optional.empty();
    }

    public Optional<WebElement> tryFindElement(By by) {
        return driver.findElements(by).stream().findFirst();
    }

    private Optional<WebElement> tryFindDisplayedElement(By by) {
        return driver.findElements(by).stream().filter(WebElement::isDisplayed).findFirst();
    }
}
