package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.searchelements;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.searchelements.BankIdElementLocator.EMPTY_LOCATOR;

import java.util.List;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.openqa.selenium.WebElement;

@Getter
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class BankIdElementsSearchResult {

    private final BankIdElementLocator locatorFound;
    private final List<WebElement> webElementsFound;

    public static BankIdElementsSearchResult of(
            BankIdElementLocator locator, List<WebElement> webElements) {
        return new BankIdElementsSearchResult(locator, webElements);
    }

    public static BankIdElementsSearchResult of(
            BankIdElementLocator locator, WebElement... webElements) {
        return new BankIdElementsSearchResult(locator, asList(webElements));
    }

    public static BankIdElementsSearchResult empty() {
        return new BankIdElementsSearchResult(EMPTY_LOCATOR, emptyList());
    }

    public boolean notEmpty() {
        return !isEmpty();
    }

    public boolean isEmpty() {
        return locatorFound == EMPTY_LOCATOR;
    }

    public Optional<WebElement> getFirstFoundElement() {
        return webElementsFound.stream().findFirst();
    }
}
