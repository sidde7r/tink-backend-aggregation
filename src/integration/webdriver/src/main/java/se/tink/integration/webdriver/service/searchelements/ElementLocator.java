package se.tink.integration.webdriver.service.searchelements;

import static se.tink.integration.webdriver.service.WebDriverConstants.EMPTY_BY;

import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.openqa.selenium.By;
import org.openqa.selenium.By.ByCssSelector;
import org.openqa.selenium.WebElement;

/**
 * Allows to find element not only by their selector but also other factors, e.g. in which iframe
 * it's embedded or what text does it contain
 */
@Getter
@ToString
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ElementLocator {

    public static final ElementLocator EMPTY_LOCATOR = ElementLocator.builder().build();

    private final By iframeSelector;
    private final By shadowDOMHostSelector;
    private final By elementSelector;
    private final List<ElementFilter> additionalFilters;

    public static Builder builder() {
        return new Builder();
    }

    public boolean matchesAdditionalFilters(WebElement element) {
        return additionalFilters.stream().allMatch(filter -> filter.matches(element));
    }

    public static class Builder {

        private By iframeSelector = EMPTY_BY;
        private By shadowHostSelector = EMPTY_BY;
        private By elementSelector = EMPTY_BY;

        private final List<ElementFilter> additionalFilters = new ArrayList<>();

        public Builder iframe(By iframeSelector) {
            this.iframeSelector = iframeSelector;
            return this;
        }

        public Builder shadowHost(By shadowHostSelector) {
            this.shadowHostSelector = shadowHostSelector;
            return this;
        }

        /**
         * @param elementSelector - needs to be a CSS selector because of browser limitations when
         *     it comes to searching elements in shadow DOM
         */
        public Builder element(ByCssSelector elementSelector) {
            this.elementSelector = elementSelector;
            return this;
        }

        public Builder mustBeDisplayed() {
            this.additionalFilters.add(new ElementFilters.ElementIsDisplayedFilter());
            return this;
        }

        public Builder mustContainOneOfTexts(String... texts) {
            this.additionalFilters.add(ElementFilters.ElementContainsTextFilter.of(texts));
            return this;
        }

        public Builder mustHaveExactText(String text) {
            this.additionalFilters.add(ElementFilters.ElementHasExactTextFilter.of(text));
            return this;
        }

        public ElementLocator build() {
            return new ElementLocator(
                    iframeSelector, shadowHostSelector, elementSelector, additionalFilters);
        }
    }
}
