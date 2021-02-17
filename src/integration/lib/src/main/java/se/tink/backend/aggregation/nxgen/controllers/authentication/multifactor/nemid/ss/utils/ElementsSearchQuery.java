package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.utils;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import org.openqa.selenium.By;

@Data
public class ElementsSearchQuery {

    private static final Integer DEFAULT_SEARCH_TIMEOUT_IN_SECONDS = 10;

    private final List<By> elementsInParentWindow;
    private final List<By> elementsInAnIframe;
    private final Integer timeoutInSeconds;

    public static ElementsSearchQueryBuilder builder() {
        return new ElementsSearchQueryBuilder();
    }

    public static class ElementsSearchQueryBuilder {

        private final List<By> elementsInParentWindow = new ArrayList<>();
        private final List<By> elementsInIframe = new ArrayList<>();
        private Integer timeoutInSeconds = DEFAULT_SEARCH_TIMEOUT_IN_SECONDS;

        public ElementsSearchQueryBuilder searchInParentWindow(By... elements) {
            elementsInParentWindow.addAll(asList(elements));
            return this;
        }

        public ElementsSearchQueryBuilder searchInAnIframe(By... elements) {
            elementsInIframe.addAll(asList(elements));
            return this;
        }

        public ElementsSearchQueryBuilder searchInAnIframe(List<By> elements) {
            elementsInIframe.addAll(elements);
            return this;
        }

        public ElementsSearchQueryBuilder searchForSeconds(Integer seconds) {
            timeoutInSeconds = seconds;
            return this;
        }

        public ElementsSearchQuery build() {
            return new ElementsSearchQuery(
                    elementsInParentWindow, elementsInIframe, timeoutInSeconds);
        }
    }
}
