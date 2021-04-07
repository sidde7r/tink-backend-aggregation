package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.searchelements;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdConstants;

@Getter
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class BankIdElementsSearchQuery {

    private final List<BankIdElementLocator> locators;

    private final boolean searchOnlyOnce;
    private final Integer timeoutInSeconds;

    public static ElementsSearchQueryBuilder builder() {
        return new ElementsSearchQueryBuilder();
    }

    public static class ElementsSearchQueryBuilder {

        private final List<BankIdElementLocator> elements = new ArrayList<>();

        private boolean searchOnlyOnce = false;
        private Integer timeoutInSeconds =
                BankIdConstants.DEFAULT_WAIT_FOR_ELEMENT_TIMEOUT_IN_SECONDS;

        public ElementsSearchQueryBuilder searchFor(BankIdElementLocator... locators) {
            this.elements.addAll(asList(locators));
            return this;
        }

        public ElementsSearchQueryBuilder searchFor(Collection<BankIdElementLocator> locators) {
            this.elements.addAll(locators);
            return this;
        }

        public ElementsSearchQueryBuilder searchForSeconds(Integer seconds) {
            timeoutInSeconds = seconds;
            return this;
        }

        public ElementsSearchQueryBuilder searchOnlyOnce(boolean searchOnlyOnce) {
            this.searchOnlyOnce = searchOnlyOnce;
            return this;
        }

        public BankIdElementsSearchQuery build() {
            return new BankIdElementsSearchQuery(elements, searchOnlyOnce, timeoutInSeconds);
        }
    }
}
