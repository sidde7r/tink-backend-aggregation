package se.tink.integration.webdriver.service.searchelements;

import static se.tink.integration.webdriver.service.WebDriverConstants.EMPTY_BY;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.By.ByCssSelector;
import org.openqa.selenium.WebElement;
import se.tink.integration.webdriver.WebDriverWrapper;
import se.tink.integration.webdriver.service.basicutils.WebDriverBasicUtils;

/**
 * Allows to find element not only by their selector but also other factors, e.g. in which iframe
 * it's embedded or what text does it contain
 */
@Getter
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ElementLocator {

    public static final ElementLocator EMPTY_LOCATOR = ElementLocator.builder().build();

    private final List<By> iframeSelectors;
    private final By shadowDOMHostSelector;
    private final By elementSelector;
    private final List<ElementFilter> additionalFilters;

    public static Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(
                iframeSelectors, shadowDOMHostSelector, elementSelector, additionalFilters);
    }

    public boolean matchesAdditionalFilters(
            WebElement element, WebDriverWrapper driver, WebDriverBasicUtils basicUtils) {
        return additionalFilters.stream()
                .allMatch(filter -> filter.matches(element, driver, basicUtils));
    }

    @AllArgsConstructor
    public static class Builder {

        private final List<By> iframeSelectors;
        private By shadowHostSelector = EMPTY_BY;
        private By elementSelector = EMPTY_BY;

        private final List<ElementFilter> additionalFilters;

        public Builder() {
            this.iframeSelectors = new ArrayList<>();
            this.additionalFilters = new ArrayList<>();
        }

        public Builder topMostIframe(By iframeSelector) {
            this.iframeSelectors.add(0, iframeSelector);
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

        public Builder element(By elementSelector) {
            this.elementSelector = elementSelector;
            return this;
        }

        public Builder mustBeDisplayed() {
            this.additionalFilters.add(new ElementFilters.ElementIsDisplayedFilter());
            return this;
        }

        public Builder mustBeVisible() {
            this.additionalFilters.add(new ElementFilters.ElementIsVisibleFilter());
            return this;
        }

        public Builder mustComplyWithFilter(ElementFilter filter) {
            this.additionalFilters.add(filter);
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

        public Builder mustHaveTextMatching(Predicate<String> textMatcher) {
            this.additionalFilters.add(ElementFilters.ElementCustomTextFilter.of(textMatcher));
            return this;
        }

        public Builder mustContainAnyTextIgnoreCase(String... texts) {
            this.additionalFilters.add(
                    new ElementFilters.ElementCustomTextFilter(
                            elementText -> containsAnyTextIgnoreCase(elementText, texts)));
            return this;
        }

        public Builder mustHaveAttributeContainAnyTextLowerCase(
                String attributeName, String... texts) {
            this.additionalFilters.add(
                    (element, driverWrapper, basicUtils) ->
                            containsAnyTextIgnoreCase(element.getAttribute(attributeName), texts));
            return this;
        }

        private static boolean containsAnyTextIgnoreCase(String value, String... texts) {
            if (StringUtils.isEmpty(value)) {
                return false;
            }
            return Stream.of(texts)
                    .filter(Objects::nonNull)
                    .anyMatch(text -> StringUtils.containsIgnoreCase(value, text));
        }

        public ElementLocator build() {
            return new ElementLocator(
                    iframeSelectors, shadowHostSelector, elementSelector, additionalFilters);
        }
    }
}
