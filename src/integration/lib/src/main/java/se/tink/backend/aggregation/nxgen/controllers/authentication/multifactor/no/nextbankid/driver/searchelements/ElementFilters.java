package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.searchelements;

import static java.util.Arrays.asList;

import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.openqa.selenium.WebElement;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ElementFilters {

    @RequiredArgsConstructor
    public static class ElementIsDisplayedFilter implements ElementFilter {

        @Override
        public boolean matches(WebElement element) {
            return element.isDisplayed();
        }
    }

    @RequiredArgsConstructor
    public static class ElementContainsTextFilter implements ElementFilter {

        private final List<String> textsToContain;

        static ElementContainsTextFilter of(String... textsToContain) {
            return new ElementContainsTextFilter(asList(textsToContain));
        }

        @Override
        public boolean matches(WebElement element) {
            // in contrast to WebElement.getText(), the "textContent" attribute returns text also
            // for elements that are not displayed
            String elementText = element.getAttribute("textContent");
            return textsToContain.stream().anyMatch(elementText::contains);
        }
    }

    @RequiredArgsConstructor
    public static class ElementHasExactTextFilter implements ElementFilter {

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
