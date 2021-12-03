package se.tink.integration.webdriver.service.searchelements;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static se.tink.integration.webdriver.service.searchelements.ElementLocator.EMPTY_LOCATOR;

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
public class ElementsSearchResult {

    private final ElementLocator locatorFound;
    private final List<WebElement> webElementsFound;

    public static ElementsSearchResult of(ElementLocator locator, List<WebElement> webElements) {
        return new ElementsSearchResult(locator, webElements);
    }

    public static ElementsSearchResult of(ElementLocator locator, WebElement... webElements) {
        return new ElementsSearchResult(locator, asList(webElements));
    }

    public static ElementsSearchResult empty() {
        return new ElementsSearchResult(EMPTY_LOCATOR, emptyList());
    }

    public boolean isNotEmpty() {
        return !isEmpty();
    }

    public boolean isEmpty() {
        return locatorFound == EMPTY_LOCATOR;
    }

    public Optional<WebElement> getFirstFoundElement() {
        return webElementsFound.stream().findFirst();
    }
}
