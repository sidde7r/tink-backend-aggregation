package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.utils;

import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants.HtmlElements.IFRAME;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchFrameException;
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
                            try {
                                driver.switchTo().frame(element);
                                return true;
                            } catch (NoSuchFrameException e) {
                                log.warn("[NemId] Couldn't switch to iFrame");
                                return false;
                            }
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

    public ElementsSearchResult searchForFirstElement(ElementsSearchQuery query) {

        for (int i = 0; i < query.getTimeoutInSeconds(); i++) {

            switchToParentWindow();

            ElementsSearchResult parentWindowResult =
                    searchForFirstElement(query.getElementsInParentWindow());
            if (parentWindowResult.notEmpty()) {
                return parentWindowResult;
            }

            boolean hasSwitchedToAnIframe = trySwitchToNemIdIframe();

            if (hasSwitchedToAnIframe) {
                ElementsSearchResult iframeResult =
                        searchForFirstElement(query.getElementsInAnIframe());
                if (iframeResult.notEmpty()) {
                    return iframeResult;
                }
            }

            sleepFor(1_000);
        }

        return ElementsSearchResult.empty();
    }

    private ElementsSearchResult searchForFirstElement(List<By> elementSelectors) {
        for (By by : elementSelectors) {
            Optional<WebElement> maybeElementFound = tryFindElement(by);
            if (maybeElementFound.isPresent()) {
                return ElementsSearchResult.of(by, maybeElementFound.get());
            }
        }
        return ElementsSearchResult.empty();
    }

    public MultipleElementsSearchResult searchForAllElements(ElementsSearchQuery query) {

        List<ElementsSearchResult> elementsSearchResults = new ArrayList<>();

        for (int i = 0; i < query.getTimeoutInSeconds(); i++) {

            switchToParentWindow();
            elementsSearchResults.addAll(searchForAllElements(query.getElementsInParentWindow()));

            boolean hasSwitchedToAnIframe = trySwitchToNemIdIframe();
            if (hasSwitchedToAnIframe) {
                elementsSearchResults.addAll(searchForAllElements(query.getElementsInAnIframe()));
            }

            // if at least one element was found we assume that the page was refreshed and all other
            // elements should've also been found - otherwise they don't exists
            if (!elementsSearchResults.isEmpty()) {
                return MultipleElementsSearchResult.of(elementsSearchResults);
            }

            sleepFor(1_000);
        }

        return MultipleElementsSearchResult.empty();
    }

    private List<ElementsSearchResult> searchForAllElements(List<By> elementSelectors) {
        List<ElementsSearchResult> searchResults = new ArrayList<>();

        for (By by : elementSelectors) {
            Optional<WebElement> maybeElementFound = tryFindElement(by);
            maybeElementFound.ifPresent(
                    element -> searchResults.add(ElementsSearchResult.of(by, element)));
        }

        return searchResults;
    }

    public Optional<WebElement> tryFindElement(By by) {
        return driver.findElements(by).stream().findFirst();
    }

    private Optional<WebElement> tryFindDisplayedElement(By by) {
        return driver.findElements(by).stream().filter(WebElement::isDisplayed).findFirst();
    }
}
