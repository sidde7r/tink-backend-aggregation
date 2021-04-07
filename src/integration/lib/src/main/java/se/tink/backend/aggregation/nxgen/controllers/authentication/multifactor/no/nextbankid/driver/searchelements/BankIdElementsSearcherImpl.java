package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.searchelements;

import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.BankIdWebDriverConstants.EMPTY_BY;

import com.google.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.utils.BankIdWebDriverCommonUtils;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.utils.Sleeper;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class BankIdElementsSearcherImpl implements BankIdElementsSearcher {

    private final WebDriver driver;
    private final JavascriptExecutor javascriptExecutor;
    private final BankIdWebDriverCommonUtils driverCommonUtils;
    private final Sleeper sleeper;

    @Override
    public BankIdElementsSearchResult searchForFirstMatchingLocator(
            BankIdElementsSearchQuery query) {

        if (query.isSearchOnlyOnce()) {
            return searchForFirstMatchingLocator(query.getLocators());
        }

        for (int i = 0; i < query.getTimeoutInSeconds(); i++) {

            BankIdElementsSearchResult searchResult =
                    searchForFirstMatchingLocator(query.getLocators());
            if (searchResult.isNotEmpty()) {
                return searchResult;
            }
            sleeper.sleepFor(1_000);
        }

        return BankIdElementsSearchResult.empty();
    }

    private BankIdElementsSearchResult searchForFirstMatchingLocator(
            List<BankIdElementLocator> locators) {
        for (BankIdElementLocator locator : locators) {

            BankIdElementsSearchResult searchResult = searchForLocator(locator);
            if (searchResult.isNotEmpty()) {
                return searchResult;
            }
        }
        return BankIdElementsSearchResult.empty();
    }

    private BankIdElementsSearchResult searchForLocator(BankIdElementLocator locator) {
        if (!trySwitchWindowForLocator(locator)) {
            return BankIdElementsSearchResult.empty();
        }

        List<WebElement> foundElements =
                tryFindSearchContextForLocator(locator)
                        .map(searchContext -> tryFindElements(searchContext, locator))
                        .orElse(Collections.emptyList());

        if (foundElements.isEmpty()) {
            return BankIdElementsSearchResult.empty();
        }
        return BankIdElementsSearchResult.of(locator, foundElements);
    }

    private boolean trySwitchWindowForLocator(BankIdElementLocator locator) {
        By iframeSelector = locator.getIframeSelector();

        if (iframeSelector == EMPTY_BY) {
            driverCommonUtils.switchToParentWindow();
            return true;
        }

        driverCommonUtils.switchToParentWindow();
        return driverCommonUtils.trySwitchToIframe(iframeSelector);
    }

    private Optional<? extends SearchContext> tryFindSearchContextForLocator(
            BankIdElementLocator locator) {
        By shadowHost = locator.getShadowDOMHostSelector();

        if (shadowHost == EMPTY_BY) {
            // use main DOM i.e. a driver itself
            return Optional.of(driver);
        }
        // use host element "shadowRoot" property
        return tryFindElement(shadowHost).map(this::getShadowDomRoot);
    }

    private @Nullable WebElement getShadowDomRoot(WebElement shadowHost) {
        return (WebElement)
                javascriptExecutor.executeScript("return arguments[0].shadowRoot", shadowHost);
    }

    private List<WebElement> tryFindElements(
            SearchContext searchContext, BankIdElementLocator locator) {

        return searchContext.findElements(locator.getElementSelector()).stream()
                .filter(locator::matchesAdditionalFilters)
                .collect(Collectors.toList());
    }

    private Optional<WebElement> tryFindElement(By by) {
        return driver.findElements(by).stream().findFirst();
    }
}
