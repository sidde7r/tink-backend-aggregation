package se.tink.integration.webdriver.service.searchelements;

import java.util.List;

public interface ElementsSearcher {

    /**
     * Search for first existing locator from all locators defined in search query.
     *
     * <p>Note that {@link ElementsSearchResult} may contain more than one {@link
     * org.openqa.selenium.WebElement} if all of them match the locator that was found.
     */
    ElementsSearchResult searchForFirstMatchingLocator(ElementsSearchQuery query);

    /**
     * Search for all existing locators from locators defined in search query.
     *
     * <p>Note that {@link ElementsSearchResult} may contain more than one {@link
     * org.openqa.selenium.WebElement} if all of them match the locator that was found.
     */
    List<ElementsSearchResult> searchForAllMatchingLocators(ElementsSearchQuery query);
}
