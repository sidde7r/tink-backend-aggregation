package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.utils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.Data;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;

@Data
public class ElementsSearchResult {

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

    public boolean notEmpty() {
        return selector != EMPTY_BY;
    }

    public String getElementTextTrimmed() {
        return Optional.ofNullable(webElement)
                .map(WebElement::getText)
                .map(String::trim)
                .orElseThrow(() -> new IllegalStateException("Web element is null"));
    }
}
