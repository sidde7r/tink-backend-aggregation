package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.searchelements;

public interface BankIdElementsSearcher {

    /**
     * Search for first existing locator from all locators defined in search query.
     *
     * <p>Note that {@link BankIdElementsSearchResult} may contain more than one {@link
     * org.openqa.selenium.WebElement} if all of them match the locator that was found.
     */
    BankIdElementsSearchResult searchForFirstMatchingLocator(BankIdElementsSearchQuery query);
}
