package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.searchelements;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.openqa.selenium.WebElement;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BankIdElementFilters {

    @RequiredArgsConstructor
    public static class ElementIsDisplayedFilter implements BankIdElementFilter {

        @Override
        public boolean matches(WebElement element) {
            return element.isDisplayed();
        }
    }

    @RequiredArgsConstructor
    public static class ElementContainsTextFilter implements BankIdElementFilter {

        private final String textToContain;

        static ElementContainsTextFilter of(String textToContain) {
            return new ElementContainsTextFilter(textToContain);
        }

        @Override
        public boolean matches(WebElement element) {
            // in contrast to WebElement.getText(), the "textContent" attribute returns text also
            // for elements that are not displayed
            return element.getAttribute("textContent").contains(textToContain);
        }
    }

    @RequiredArgsConstructor
    public static class ElementHasExactTextFilter implements BankIdElementFilter {

        private final String text;

        static ElementHasExactTextFilter of(String textToContain) {
            return new ElementHasExactTextFilter(textToContain);
        }

        @Override
        public boolean matches(WebElement element) {
            return element.getAttribute("textContent").equals(text);
        }
    }
}
