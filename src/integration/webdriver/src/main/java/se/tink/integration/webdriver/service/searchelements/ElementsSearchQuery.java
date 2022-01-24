package se.tink.integration.webdriver.service.searchelements;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import se.tink.integration.webdriver.service.WebDriverConstants;

@Data
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ElementsSearchQuery {

    private final List<ElementLocator> locators;
    /**
     * If this value is 0, there will be only one search performed. If this value is > 0, number of
     * searches depends on how many searches per second are run by {@link ElementsSearcher}.
     */
    private final int searchForSeconds;

    public static ElementsSearchQueryBuilder builder() {
        return new ElementsSearchQueryBuilder();
    }

    public static class ElementsSearchQueryBuilder {

        private final List<ElementLocator> elements = new ArrayList<>();
        private Integer searchForSeconds =
                WebDriverConstants.DEFAULT_WAIT_FOR_ELEMENT_TIMEOUT_IN_SECONDS;
        private boolean searchOnlyOnce;

        public ElementsSearchQueryBuilder searchFor(ElementLocator... locators) {
            this.elements.addAll(asList(locators));
            return this;
        }

        public ElementsSearchQueryBuilder searchFor(Collection<ElementLocator> locators) {
            this.elements.addAll(locators);
            return this;
        }

        public ElementsSearchQueryBuilder searchForSeconds(Integer searchForSeconds) {
            this.searchForSeconds = searchForSeconds;
            return this;
        }

        public ElementsSearchQueryBuilder searchOnlyOnce() {
            this.searchForSeconds = 0;
            return this;
        }

        public ElementsSearchQuery build() {
            return new ElementsSearchQuery(elements, searchForSeconds);
        }
    }
}
