package se.tink.integration.webdriver.service.searchelements;

import static java.util.Arrays.asList;

import java.util.List;
import java.util.function.Predicate;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.openqa.selenium.WebElement;
import se.tink.integration.webdriver.WebDriverWrapper;
import se.tink.integration.webdriver.service.basicutils.WebDriverBasicUtils;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ElementFilters {

    @RequiredArgsConstructor
    public static class ElementIsVisibleFilter implements ElementFilter {

        @Override
        public boolean matches(
                WebElement element,
                WebDriverWrapper driverWrapper,
                WebDriverBasicUtils basicUtils) {
            return basicUtils.isElementVisible(element);
        }
    }

    @RequiredArgsConstructor
    public static class ElementIsDisplayedFilter implements ElementFilter {

        @Override
        public boolean matches(
                WebElement element,
                WebDriverWrapper driverWrapper,
                WebDriverBasicUtils basicUtils) {
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
        public boolean matches(
                WebElement element,
                WebDriverWrapper driverWrapper,
                WebDriverBasicUtils basicUtils) {
            String elementText = getElementTextContent(element);
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
        public boolean matches(
                WebElement element,
                WebDriverWrapper driverWrapper,
                WebDriverBasicUtils basicUtils) {
            return getElementTextContent(element).equals(text);
        }
    }

    @RequiredArgsConstructor
    public static class ElementCustomTextFilter implements ElementFilter {

        private final Predicate<String> customMatcher;

        static ElementCustomTextFilter of(Predicate<String> customMatcher) {
            return new ElementCustomTextFilter(customMatcher);
        }

        @Override
        public boolean matches(
                WebElement element,
                WebDriverWrapper driverWrapper,
                WebDriverBasicUtils basicUtils) {
            return customMatcher.test(getElementTextContent(element));
        }
    }

    /**
     * In contrast to WebElement.getText(), the "textContent" attribute returns text also for
     * elements that are not displayed.
     */
    private static String getElementTextContent(WebElement element) {
        return element.getAttribute("textContent");
    }
}
