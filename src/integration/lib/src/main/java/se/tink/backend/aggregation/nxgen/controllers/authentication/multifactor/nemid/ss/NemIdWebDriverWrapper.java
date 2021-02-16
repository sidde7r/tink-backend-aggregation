package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss;

import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants.HtmlElements.IFRAME;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

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
                                    throw new IllegalStateException(
                                            "Could not find element by " + by);
                                });
        element.sendKeys(value);
    }

    public void clickButton(By by) {
        tryFindDisplayedElement(by)
                .orElseThrow(
                        () -> new IllegalStateException("Could not find button element by " + by))
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

    public ElementsSearchResult searchForFirstElement(
            List<By> elementSelectors, int timeoutInSeconds) {

        for (int i = 0; i < timeoutInSeconds; i++) {

            Optional<ElementsSearchResult> maybeFindResult =
                    searchForFirstElement(elementSelectors);
            if (maybeFindResult.isPresent()) {
                return maybeFindResult.get();
            }
            sleepFor(1_000);
        }
        return ElementsSearchResult.empty();
    }

    private Optional<ElementsSearchResult> searchForFirstElement(List<By> elementSelectors) {
        for (By by : elementSelectors) {
            Optional<WebElement> maybeElementFound = tryFindElement(by);
            if (maybeElementFound.isPresent()) {
                return Optional.of(ElementsSearchResult.of(by, maybeElementFound.get()));
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

    @Data
    public static class ElementsSearchResult {

        private static final By EMPTY_BY =
                new By() {
                    @Override
                    public List<WebElement> findElements(SearchContext context) {
                        return Collections.emptyList();
                    }
                };

        private final By selector;
        private final WebElement webElement;

        public static ElementsSearchResult of(By selector, WebElement webElement) {
            return new ElementsSearchResult(selector, webElement);
        }

        public static ElementsSearchResult empty() {
            return new ElementsSearchResult(EMPTY_BY, null);
        }
    }
}
