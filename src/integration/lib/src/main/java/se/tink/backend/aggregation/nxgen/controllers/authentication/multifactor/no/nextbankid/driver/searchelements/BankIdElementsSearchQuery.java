package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.searchelements;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdConstants;

@Data
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class BankIdElementsSearchQuery {

    private final List<BankIdElementLocator> locators;
    /**
     * If this value is 0, there will be only one search performed. If this value is > 0, number of
     * searches depends on how many searches per second are run by {@link BankIdElementsSearcher}.
     */
    private final Integer searchForSeconds;

    private final boolean searchOnlyOnce;

    public static ElementsSearchQueryBuilder builder() {
        return new ElementsSearchQueryBuilder();
    }

    public static class ElementsSearchQueryBuilder {

        private final List<BankIdElementLocator> elements = new ArrayList<>();
        private Integer searchForSeconds =
                BankIdConstants.DEFAULT_WAIT_FOR_ELEMENT_TIMEOUT_IN_SECONDS;
        private boolean searchOnlyOnce;

        public ElementsSearchQueryBuilder searchFor(BankIdElementLocator... locators) {
            this.elements.addAll(asList(locators));
            return this;
        }

        public ElementsSearchQueryBuilder searchFor(Collection<BankIdElementLocator> locators) {
            this.elements.addAll(locators);
            return this;
        }

        public ElementsSearchQueryBuilder searchForSeconds(Integer seconds) {
            searchForSeconds = seconds;
            return this;
        }

        public ElementsSearchQueryBuilder searchOnlyOnce() {
            searchOnlyOnce = true;
            return this;
        }

        public BankIdElementsSearchQuery build() {
            return new BankIdElementsSearchQuery(elements, searchForSeconds, searchOnlyOnce);
        }
    }
}
