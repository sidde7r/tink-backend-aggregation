package se.tink.integration.webdriver.service.searchelements;

import static se.tink.integration.webdriver.service.WebDriverConstants.EMPTY_BY;

import com.google.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import se.tink.integration.webdriver.WebDriverWrapper;
import se.tink.integration.webdriver.service.basicutils.WebDriverBasicUtils;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class ElementsSearcherImpl implements ElementsSearcher {

    private final WebDriverWrapper driver;
    private final WebDriverBasicUtils basicUtils;

    @Override
    public ElementsSearchResult searchForFirstMatchingLocator(ElementsSearchQuery query) {
        // always search at least once
        ElementsSearchResult firstSearchResult = searchForFirstMatchingLocator(query.getLocators());
        if (firstSearchResult.isNotEmpty()) {
            return firstSearchResult;
        }

        int secondsSlept = 0;
        while (secondsSlept++ < query.getSearchForSeconds()) {

            ElementsSearchResult searchResult = searchForFirstMatchingLocator(query.getLocators());
            if (searchResult.isNotEmpty()) {
                return searchResult;
            }

            basicUtils.sleepFor(1_000);
        }

        return ElementsSearchResult.empty();
    }

    @Override
    public List<ElementsSearchResult> searchForAllMatchingLocators(ElementsSearchQuery query) {
        // always search at least once
        List<ElementsSearchResult> firstSearchResult =
                searchForAllMatchingLocators(query.getLocators());
        // if at least one element was found, we assume that the page was already refreshed and all
        // other elements should've been visible, if they are present on this page
        if (!firstSearchResult.isEmpty()) {
            return firstSearchResult;
        }

        int secondsSlept = 0;
        while (secondsSlept++ < query.getSearchForSeconds()) {

            List<ElementsSearchResult> searchResult =
                    searchForAllMatchingLocators(query.getLocators());
            // same assumption here - if 1 element is found, other elements should've
            // also been found
            if (!searchResult.isEmpty()) {
                return searchResult;
            }

            basicUtils.sleepFor(1_000);
        }

        return Collections.emptyList();
    }

    private List<ElementsSearchResult> searchForAllMatchingLocators(List<ElementLocator> locators) {
        return locators.stream()
                .map(this::searchForLocator)
                .filter(ElementsSearchResult::isNotEmpty)
                .collect(Collectors.toList());
    }

    private ElementsSearchResult searchForFirstMatchingLocator(List<ElementLocator> locators) {
        for (ElementLocator locator : locators) {

            ElementsSearchResult searchResult = searchForLocator(locator);
            if (searchResult.isNotEmpty()) {
                return searchResult;
            }
        }
        return ElementsSearchResult.empty();
    }

    private ElementsSearchResult searchForLocator(ElementLocator locator) {
        if (!trySwitchWindowForLocator(locator)) {
            return ElementsSearchResult.empty();
        }

        List<WebElement> foundElements =
                tryFindSearchContextForLocator(locator)
                        .map(searchContext -> tryFindElements(searchContext, locator))
                        .orElse(Collections.emptyList());

        if (foundElements.isEmpty()) {
            return ElementsSearchResult.empty();
        }
        return ElementsSearchResult.of(locator, foundElements);
    }

    private boolean trySwitchWindowForLocator(ElementLocator locator) {
        basicUtils.switchToParentWindow();
        return locator.getIframeSelectors().stream()
                .map(basicUtils::trySwitchToIframe)
                .reduce(true, Boolean::logicalAnd);
    }

    private Optional<? extends SearchContext> tryFindSearchContextForLocator(
            ElementLocator locator) {
        By shadowHost = locator.getShadowDOMHostSelector();

        if (shadowHost == EMPTY_BY) {
            // use main DOM i.e. a driver itself
            return Optional.of(driver);
        }
        // use host element "shadowRoot" property
        return tryFindElement(shadowHost).map(this::getShadowDomRoot);
    }

    private @Nullable WebElement getShadowDomRoot(WebElement shadowHost) {
        return (WebElement) driver.executeScript("return arguments[0].shadowRoot", shadowHost);
    }

    private List<WebElement> tryFindElements(SearchContext searchContext, ElementLocator locator) {

        return searchContext.findElements(locator.getElementSelector()).stream()
                .filter(element -> doesElementMatchAdditionalFilters(element, locator))
                .collect(Collectors.toList());
    }

    private boolean doesElementMatchAdditionalFilters(WebElement element, ElementLocator locator) {
        try {
            return locator.matchesAdditionalFilters(element, driver, basicUtils);

        } catch (StaleElementReferenceException e) {
            /*
            Sometimes right after we find element it can be removed from DOM due to some style recalculations or just
            dynamic loading of a different view. If this happens before we run additional filters we can get stale
            element reference exception - e.g. we check the text of non existing element. Since this element was
            just removed we return false to not include it in search result.
             */
            return false;
        }
    }

    private Optional<WebElement> tryFindElement(By by) {
        return driver.findElements(by).stream().findFirst();
    }
}
