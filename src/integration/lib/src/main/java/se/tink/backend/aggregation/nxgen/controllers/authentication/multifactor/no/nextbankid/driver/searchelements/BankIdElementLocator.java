package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.searchelements;

import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.BankIdWebDriverConstants.EMPTY_BY;

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
public class BankIdElementLocator {

    public static final BankIdElementLocator EMPTY_LOCATOR = BankIdElementLocator.builder().build();

    private final By iframeSelector;
    private final By shadowDOMHostSelector;
    private final By elementSelector;
    private final List<BankIdElementFilter> additionalFilters;

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

        private final List<BankIdElementFilter> additionalFilters = new ArrayList<>();

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
            this.additionalFilters.add(new BankIdElementFilters.ElementIsDisplayedFilter());
            return this;
        }

        public Builder mustContainText(String text) {
            this.additionalFilters.add(BankIdElementFilters.ElementContainsTextFilter.of(text));
            return this;
        }

        public Builder mustHaveExactText(String text) {
            this.additionalFilters.add(BankIdElementFilters.ElementHasExactTextFilter.of(text));
            return this;
        }

        public BankIdElementLocator build() {
            return new BankIdElementLocator(
                    iframeSelector, shadowHostSelector, elementSelector, additionalFilters);
        }
    }
}
