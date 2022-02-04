package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.flow.screens;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@ToString
@Getter
@EqualsAndHashCode
@RequiredArgsConstructor
public class MitIdScreenQuery {

    private final List<MitIdScreen> expectedScreensToSearchFor;
    private final int searchForSeconds;

    public static MitIdScreenQueryBuilder builder() {
        return new MitIdScreenQueryBuilder();
    }

    public MitIdScreenQueryBuilder toBuilder() {
        return new MitIdScreenQueryBuilder(this);
    }

    public static class MitIdScreenQueryBuilder {

        private final List<MitIdScreen> screensToWaitFor;
        private int searchForSeconds;

        public MitIdScreenQueryBuilder() {
            screensToWaitFor = new ArrayList<>();
            searchForSeconds = 0;
        }

        public MitIdScreenQueryBuilder(MitIdScreenQuery screenQuery) {
            this.screensToWaitFor = screenQuery.getExpectedScreensToSearchFor();
            this.searchForSeconds = screenQuery.getSearchForSeconds();
        }

        public MitIdScreenQueryBuilder searchForExpectedScreens(MitIdScreen... screens) {
            screensToWaitFor.addAll(asList(screens));
            return this;
        }

        public MitIdScreenQueryBuilder searchForExpectedScreens(List<MitIdScreen> screens) {
            screensToWaitFor.addAll(screens);
            return this;
        }

        public MitIdScreenQueryBuilder searchForSeconds(int seconds) {
            this.searchForSeconds = seconds;
            return this;
        }

        public MitIdScreenQueryBuilder searchOnlyOnce() {
            this.searchForSeconds = 0;
            return this;
        }

        public MitIdScreenQuery build() {
            return new MitIdScreenQuery(screensToWaitFor, searchForSeconds);
        }
    }
}
